package com.shop.tcd.data.dto.remains.response


import com.google.gson.annotations.SerializedName

data class RemainsItemResponse(
    @SerializedName("barcode") val barcode: String, // 10005
    @SerializedName("code") val code: String, // 10005
    @SerializedName("doc") val doc: String, // Б-00922722 от 23.10.2021 17:18:45
    @SerializedName("found") val found: Boolean, // true
    @SerializedName("name") val name: String, // GreenField
    @SerializedName("plu") val plu: Int, // 123
    @SerializedName("price") val price: Double, // 49.9
    @SerializedName("remain") val remain: Double, // 101.978
    @SerializedName("sales") val sales: Int, // 0
)