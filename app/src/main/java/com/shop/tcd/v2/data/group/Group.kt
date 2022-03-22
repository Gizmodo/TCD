package com.shop.tcd.v2.data.group

import com.google.gson.annotations.SerializedName

data class Group(
    @SerializedName("code")
    val code: String,
    @SerializedName("name")
    val name: String,
    var checked: Boolean = false,
)