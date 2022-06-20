package com.shop.tcd.data.dto.group

import com.google.gson.annotations.SerializedName

data class Group(
    @SerializedName("code")
    val code: String,
    @SerializedName("name")
    val name: String,
    var checked: Boolean = false,
)
