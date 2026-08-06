package cz.petrf.kvalitaacena.service;

import cz.petrf.kvalitaacena.config.PriceHistoryProperties;
import cz.petrf.kvalitaacena.controller.PriceHistory;
import cz.petrf.kvalitaacena.controller.PricePoint;
import cz.petrf.kvalitaacena.db.entity.PriceKind;
import cz.petrf.kvalitaacena.db.entity.Store;
import cz.petrf.kvalitaacena.db.repo.PriceDailyRepository;
import cz.petrf.kvalitaacena.db.repo.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Denní řada z agg.price_daily pro graf vývoje ceny — NIKDY ze syrových core.price_observation
 * (docs/datovy-model.md). Bez storeId je to medián mediánů přes provozovny (docs/reputace.md).
 */
@Service
@RequiredArgsConstructor
public class PriceHistoryService {

  private final PriceDailyRepository priceDailyRepository;
  private final StoreRepository storeRepository;
  private final PriceHistoryProperties properties;

  @Transactional(readOnly = true)
  public PriceHistory history(Long productId, PriceKind priceKind, Long storeId, Integer requestedDays,
      boolean authenticated) {
    int cap = authenticated ? properties.getMaxDays() : properties.getAnonymousMaxDays();
    int requested = requestedDays == null ? 90 : requestedDays;
    int days = Math.max(1, Math.min(requested, cap));
    LocalDate fromDay = LocalDate.now(ZoneOffset.UTC).minusDays(days);
    PriceKind kind = priceKind == null ? PriceKind.REGULAR : priceKind;

    if (storeId != null) {
      Store store = storeRepository.findById(storeId).orElse(null);
      List<PricePoint> points = priceDailyRepository
          .findByProductIdAndStoreIdAndPriceKindAndDayGreaterThanEqualOrderByDayAsc(productId, storeId, kind, fromDay)
          .stream()
          .map(d -> new PricePoint(d.getDay(), d.getPriceAmount(), d.getUnitPrice(), d.getNObs(), 1))
          .toList();
      return new PriceHistory(kind, store, days, points);
    }

    List<PricePoint> points = priceDailyRepository.nationalHistory(productId, kind.name(), fromDay).stream()
        .map(row -> new PricePoint(row.getDay(), row.getPriceAmount(), row.getUnitPrice(), row.getNObs(), row.getStoreCount()))
        .toList();
    return new PriceHistory(kind, null, days, points);
  }
}
