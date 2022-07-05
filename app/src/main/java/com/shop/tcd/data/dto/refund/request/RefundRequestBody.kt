package com.shop.tcd.data.dto.refund.request

import com.google.gson.annotations.SerializedName
import com.shop.tcd.data.dto.datamatrix.Goods

data class RefundRequestBody(
    @SerializedName("doctype")
    val doctype: String,
    @SerializedName("prefix")
    val prefix: String,
    @SerializedName("barcode")
    val barcode: String?,
    @SerializedName("datamatrix_raw")
    val datamatrixRaw: String,
    @SerializedName("datamatrix")
    val datamatrix: Goods?,
    @SerializedName("codestamp")
    val pdf417: String?,
)
