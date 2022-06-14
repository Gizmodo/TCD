package com.shop.tcd.data.dto.ato

import com.google.gson.annotations.SerializedName

data class UpdateResponse(
    @SerializedName("url")
    val url: String,
    @SerializedName("version")
    val version: String
)
