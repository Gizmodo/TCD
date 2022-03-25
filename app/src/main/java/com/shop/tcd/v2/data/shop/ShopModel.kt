package com.shop.tcd.v2.data.shop

import com.google.gson.annotations.SerializedName

data class ShopModel(
    @SerializedName("Адрес")
    val address: String,
    @SerializedName("Наименование")
    val name: String,
    @SerializedName("Префикс")
    val prefix: String,
    @SerializedName("Сервис")
    val service: String,
    @SerializedName("Шаблоны")
    val templates: List<String>,
    @SerializedName("ПрефиксВесовой")
    val prefixWeight: String,
    @SerializedName("ПрефиксШтучный")
    val prefixSingle: String,
    @SerializedName("ПрефиксPLU")
    val prefixPLU: String,
)