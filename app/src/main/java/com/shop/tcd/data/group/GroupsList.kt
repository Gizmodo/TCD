package com.shop.tcd.data.group

import com.google.gson.annotations.SerializedName

data class GroupsList(
    @SerializedName("group")
    val groups: List<Group>,
    val message: String,
    val result: String,
)
