package com.shop.tcd.v2.data.basket

data class Backet(
    val `data`: List<Any>,
    val deviceUserId: String,
    val message: String,
    val status: Int
)