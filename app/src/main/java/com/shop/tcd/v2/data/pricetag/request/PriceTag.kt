package com.shop.tcd.v2.data.pricetag

import com.google.gson.annotations.SerializedName

data class PriceTag(
    @SerializedName("shopid")
    val shopid: String,
    @SerializedName("barcodes")
    val barcodesList: List<BarcodeTag>,
)

data class BarcodeTag(
    @SerializedName("barcode")
    val barcode: String,
)
