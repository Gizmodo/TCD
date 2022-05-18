package com.shop.tcd.data.dto.remains.request

import com.google.gson.annotations.SerializedName

data class RemainsBarcodeFieldRequest(
    @SerializedName("barcode")
    var barcode: String,
)
