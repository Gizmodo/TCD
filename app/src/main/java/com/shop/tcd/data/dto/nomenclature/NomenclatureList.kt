package com.shop.tcd.data.dto.nomenclature

data class NomenclatureList(
    val message: String,
    val nomenclature: List<NomenclatureItem>,
    val result: String,
)