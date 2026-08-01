package cz.petrf.kvalitaacena.controller;

import cz.petrf.kvalitaacena.db.entity.PriceCurrent;
import cz.petrf.kvalitaacena.db.entity.Store;
import cz.petrf.kvalitaacena.db.repo.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

/**
 * {@code PriceCurrent} nemá JPA vztah na {@code Store} (jen {@code storeId}, viz PriceCurrent —
 * složený klíč přes @IdClass), proto je potřeba explicitní resolver místo výchozího
 * PropertyDataFetcheru.
 */
@Controller
@RequiredArgsConstructor
public class PriceCurrentGraphQlController {

  private final StoreRepository storeRepository;

  @SchemaMapping(typeName = "PriceCurrent", field = "store")
  public Store store(PriceCurrent priceCurrent) {
    return storeRepository.findById(priceCurrent.getStoreId()).orElse(null);
  }
}
