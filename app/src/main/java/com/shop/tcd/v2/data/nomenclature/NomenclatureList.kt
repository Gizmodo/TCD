package com.shop.tcd.v2.data.nomenclature

data class NomenclatureList(
    val message: String,
    val nomenclature: List<NomenclatureItem>,
    val result: String,
)