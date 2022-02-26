package com.shop.tcd.model.newsettigs

data class ShopListItem(
    val Адрес: String,
    val Наименование: String,
    val Префикс: String,
    val Сервис: String,
    val Шаблоны: List<String>
)