package cz.petrf.kvalitaacena.controller;

import cz.petrf.kvalitaacena.db.entity.Store;

import java.util.List;

public record StoreSearchResult(List<Store> items, int totalCount, boolean hasMore) {
}
