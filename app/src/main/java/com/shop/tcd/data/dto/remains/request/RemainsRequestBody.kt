package com.shop.tcd.data.dto.remains.request

import com.google.gson.annotations.SerializedName

data class RemainsRequestBody(
    @SerializedName("shopid")
    val shopid: String,
    @SerializedName("barcodes")
    val barcodesList: List<RemainsBarcodeFieldRequest>,
)
