package cz.petrf.kvalitaacena.db.repo;

import cz.petrf.kvalitaacena.db.entity.ProductCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductCodeRepository extends JpaRepository<ProductCode, Long> {

  List<ProductCode> findByProductId(Long productId);

  /** Pro dávkové resolvery (@BatchMapping Product.codes) — vyhýbá se N+1 na seznamu produktů. */
  List<ProductCode> findByProductIdIn(Collection<Long> productIds);

  /** Vnitroobchodní kódy (STORE_INTERNAL) mají stejný `code` napříč různými chain_id, proto
   *  se pro skenování bere jen GTIN — viz docs/datovy-model.md. */
  Optional<ProductCode> findFirstByCodeAndCodeType(String code, cz.petrf.kvalitaacena.db.entity.CodeType codeType);
}
