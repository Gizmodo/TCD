package com.shop.tcd.data.pricetag.response

data class PriceTagResponseItem(
    val barcode: String,
    val code: String,
    val found: Boolean,
    val manufacturer: String,
    val nodiscount: Boolean,
    val plu: Int,
    val price: Double,
    val stock: Int,
    val string1: String,
    val string2: String,
    val string3: String,
    val string4: String,
)