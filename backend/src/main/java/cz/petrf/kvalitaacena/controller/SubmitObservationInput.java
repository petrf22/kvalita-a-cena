package cz.petrf.kvalitaacena.controller;

import cz.petrf.kvalitaacena.db.entity.PriceKind;
import cz.petrf.kvalitaacena.db.entity.QuantityBasis;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SubmitObservationInput(
    Long productId,
    Long storeId,
    BigDecimal priceAmount,
    PriceKind priceKind,
    QuantityBasis quantityBasis,
    Integer multibuyQty,
    BigDecimal multibuyTotal,
    OffsetDateTime observedAt) {
}
