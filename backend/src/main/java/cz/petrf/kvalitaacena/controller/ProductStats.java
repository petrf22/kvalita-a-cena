package cz.petrf.kvalitaacena.controller;

import cz.petrf.kvalitaacena.db.entity.Store;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProductStats(
    int observationCount,
    int storeCount,
    OffsetDateTime lastObservedAt,
    BigDecimal bestPrice,
    BigDecimal bestUnitPrice,
    Store cheapestStore) {
}
