package com.shop.tcd.model.settings


import com.google.gson.annotations.SerializedName

data class Shop(
    @SerializedName("shopName")
    val shopName: String,
    @SerializedName("shopPrefix")
    val shopPrefix: String,
    @SerializedName("shopPrefixPiece")
    val shopPrefixPiece: String,
    @SerializedName("shopPrefixWeight")
    val shopPrefixWeight: String,
    @SerializedName("shopPrefixWeightPLU")
    val shopPrefixWeightPLU: String,
    @SerializedName("shopURL")
    val shopURL: String,
)