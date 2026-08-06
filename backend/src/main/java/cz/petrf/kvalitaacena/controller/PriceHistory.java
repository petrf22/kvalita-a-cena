package cz.petrf.kvalitaacena.controller;

import cz.petrf.kvalitaacena.db.entity.PriceKind;
import cz.petrf.kvalitaacena.db.entity.Store;

import java.util.List;

public record PriceHistory(PriceKind priceKind, Store store, int days, List<PricePoint> points) {
}
