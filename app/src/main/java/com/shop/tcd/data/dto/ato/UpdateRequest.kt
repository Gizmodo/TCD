package com.shop.tcd.data.dto.ato

import com.google.gson.annotations.SerializedName

data class UpdateRequest(
    @SerializedName("pkgname")
    val pkgname: String,
    @SerializedName("version")
    val version: String
)
