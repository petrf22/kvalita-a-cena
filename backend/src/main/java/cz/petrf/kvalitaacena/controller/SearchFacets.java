package cz.petrf.kvalitaacena.controller;

import cz.petrf.kvalitaacena.db.entity.Store;

import java.util.List;

public record SearchFacets(List<Store> stores, List<String> cities) {
}
