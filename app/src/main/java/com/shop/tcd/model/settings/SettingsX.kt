package com.shop.tcd.model.settings


import com.google.gson.annotations.SerializedName

data class SettingsX(
    @SerializedName("Groups")
    val groups: List<Group>,
    @SerializedName("Shops")
    val shops: List<Shop>,
)