package com.shop.tcd.model.settings


import com.google.gson.annotations.SerializedName

data class Group(
    @SerializedName("groupName")
    val groupName: String,
    @SerializedName("groupUsers")
    val groupUsers: List<GroupUser>,
)