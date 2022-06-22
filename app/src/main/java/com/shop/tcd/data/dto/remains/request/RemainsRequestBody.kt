package com.shop.tcd.data.dto.remains.request

import com.google.gson.annotations.SerializedName

data class RemainsRequestBody(
    @SerializedName("prefix")
    val prefix: String,
    @SerializedName("barcodes")
    val barcodesList: List<RemainsBarcodeFieldRequest>,
)
