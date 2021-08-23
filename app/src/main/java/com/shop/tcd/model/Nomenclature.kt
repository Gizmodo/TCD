package com.shop.tcd.model

data class Nomenclature(
    val message: String,
    val nomenclature: List<NomenclatureItem>,
    val result: String // success
)