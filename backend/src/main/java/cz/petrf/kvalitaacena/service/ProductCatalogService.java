package cz.petrf.kvalitaacena.service;

import cz.petrf.kvalitaacena.config.CatalogProperties;
import cz.petrf.kvalitaacena.controller.CreateProductInput;
import cz.petrf.kvalitaacena.db.entity.AppUser;
import cz.petrf.kvalitaacena.db.entity.Category;
import cz.petrf.kvalitaacena.db.entity.CodeType;
import cz.petrf.kvalitaacena.db.entity.Product;
import cz.petrf.kvalitaacena.db.entity.ProductCode;
import cz.petrf.kvalitaacena.db.entity.ProductStatus;
import cz.petrf.kvalitaacena.db.repo.AppUserRepository;
import cz.petrf.kvalitaacena.db.repo.CategoryRepository;
import cz.petrf.kvalitaacena.db.repo.PriceObservationRepository;
import cz.petrf.kvalitaacena.db.repo.ProductCodeRepository;
import cz.petrf.kvalitaacena.db.repo.ProductRepository;
import cz.petrf.kvalitaacena.exception.DuplicateException;
import cz.petrf.kvalitaacena.exception.NotFoundException;
import cz.petrf.kvalitaacena.exception.TooManyRequestsException;
import cz.petrf.kvalitaacena.exception.UnauthorizedException;
import cz.petrf.kvalitaacena.security.CatalogRateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Založení zboží — s naskenovaným EANem i bez něj. Bezkódové zboží ("pečivo za 45 Kč" z
 * účtenky, "brambory" z podnikové prodejny bez EANu) vzniká jako druhová položka
 * (Product.isGeneric) se statusem DRAFT, dokud ji nepotvrdí víc přispěvatelů — viz
 * docs/reputace.md, "Zboží bez čárového kódu". Confidence agregátu pro takovou položku
 * zastropovává {@link PriceAggregationService}, ne váha záznamu (ta by v buňce
 * (produkt, obchod) byla vůči váženému mediánu i Kishovu n_eff beze změny, protože
 * multiplikuje všechny záznamy stejně).
 */
@Service
@RequiredArgsConstructor
public class ProductCatalogService {

  private final ProductRepository productRepository;
  private final BrandResolutionService brandResolutionService;
  private final CategoryRepository categoryRepository;
  private final ProductCodeRepository productCodeRepository;
  private final PriceObservationRepository priceObservationRepository;
  private final AppUserRepository appUserRepository;
  private final CatalogProperties catalogProperties;
  private final CatalogRateLimiter catalogRateLimiter;
  private final DuplicateLookupService duplicateLookupService;
  private final TrustLevelService trustLevelService;

  @Transactional
  public Product create(CreateProductInput input, UUID viewerPublicUid) {
    if (viewerPublicUid == null) {
      throw new UnauthorizedException("Založení zboží vyžaduje přihlášení");
    }
    AppUser user = appUserRepository.findByPublicUid(viewerPublicUid)
        .orElseThrow(() -> new UnauthorizedException("Účet už neexistuje"));

    if (input.name() == null || input.name().isBlank()) {
      throw new IllegalArgumentException("Název zboží je povinný");
    }
    if (input.categoryId() == null) {
      throw new IllegalArgumentException("Kategorie je povinná");
    }
    if (input.unitBase() == null) {
      throw new IllegalArgumentException("Základní jednotka (kg/l/ks) je povinná");
    }
    Category category = categoryRepository.findById(input.categoryId())
        .orElseThrow(() -> new NotFoundException("Kategorie s tímto id neexistuje"));

    String code = blankToNull(input.code());
    String gtin14 = code == null ? null : GtinNormalization.toGtin14(code);
    if (gtin14 != null) {
      productCodeRepository.findFirstByCodeAndCodeType(gtin14, CodeType.GTIN).ifPresent(existing -> {
        throw new DuplicateException("Tenhle kód už v katalogu existuje", existing.getProduct().getId());
      });
    }
    if (!catalogRateLimiter.tryAcquireProductCreation(viewerPublicUid)) {
      throw new TooManyRequestsException();
    }

    boolean variableWeight = input.isVariableWeight() != null && input.isVariableWeight();
    BigDecimal netContentBase = NetContentCalculator.computeNetContentBase(input.unitBase(),
        input.netContentValue(), input.netContentUom(), variableWeight);
    boolean generic = gtin14 == null;
    boolean trusted = trustLevelService.isTrusted(user);

    Product product = Product.builder()
        .name(input.name().trim())
        .brand(brandResolutionService.resolve(input.brandName()))
        .category(category)
        .unitBase(input.unitBase())
        .netContentValue(input.netContentValue())
        .netContentUom(input.netContentUom())
        .netContentBase(netContentBase)
        .piecesInPack(input.piecesInPack())
        .variableWeight(variableWeight)
        .generic(generic)
        // DRAFT ze dvou nezávislých důvodů, oba se řeší stejnou promocí (promoteIfConfirmed):
        // bezkódová položka VŽDY (kód sám je dost silná identifikace, aby duplicitu nešlo
        // snadno založit znovu), zboží od nedůvěryhodného autora taky (docs/reputace.md,
        // práh důvěry jako etapa-1 aproximace T2) — dokud ho nepotvrdí víc přispěvatelů.
        .status(generic || !trusted ? ProductStatus.DRAFT : ProductStatus.ACTIVE)
        .createdByUserId(user.getId())
        .build();

    try {
      product = productRepository.saveAndFlush(product);
    } catch (DataIntegrityViolationException e) {
      throw duplicateGenericOf(input.name().trim(), category.getId());
    }

    if (gtin14 != null) {
      productCodeRepository.save(ProductCode.builder()
          .product(product)
          .code(gtin14)
          .codeType(CodeType.GTIN)
          .primary(true)
          .build());
    }

    return product;
  }

  /**
   * Volá {@link PriceObservationService#submit} po každém zápisu ceny k DRAFT produktu —
   * jakmile ho potvrdí dost RŮZNÝCH přispěvatelů (app.catalog.draft-confirmations), překlopí
   * se na ACTIVE. Žádný plánovač navíc, kontrola je jen dotaz + případný jeden UPDATE.
   */
  @Transactional
  public void promoteIfConfirmed(Long productId) {
    Product product = productRepository.findById(productId).orElse(null);
    if (product == null || product.getStatus() != ProductStatus.DRAFT) return;

    long contributors = priceObservationRepository.countDistinctContributors(productId);
    if (contributors >= catalogProperties.getDraftConfirmations()) {
      product.setStatus(ProductStatus.ACTIVE);
      productRepository.save(product);
    }
  }

  private DuplicateException duplicateGenericOf(String name, Long categoryId) {
    // Vlastní transakce (DuplicateLookupService) — viz StoreService.duplicateOf, stejný důvod.
    List<Product> similar = duplicateLookupService.findSimilarProducts(name);
    Long existingId = similar.isEmpty() ? null : similar.get(0).getId();
    return new DuplicateException("Tahle druhová položka už v katalogu existuje", existingId);
  }

  private String blankToNull(String value) {
    return (value == null || value.isBlank()) ? null : value.trim();
  }
}
