package cz.petrf.kvalitaacena.controller;

import cz.petrf.kvalitaacena.db.entity.NetContentUom;
import cz.petrf.kvalitaacena.db.entity.UnitBase;

import java.math.BigDecimal;

public record CreateProductInput(
    String name,
    String brandName,
    Long categoryId,
    UnitBase unitBase,
    BigDecimal netContentValue,
    NetContentUom netContentUom,
    Integer piecesInPack,
    Boolean isVariableWeight,
    String code) {
}
