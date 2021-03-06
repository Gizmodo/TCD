package com.shop.tcd.data.dto.shop

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
)