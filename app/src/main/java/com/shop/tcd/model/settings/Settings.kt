package com.shop.tcd.model.settings


import com.google.gson.annotations.SerializedName

data class Settings(
    @SerializedName("Settings")
    val settings: SettingsX,
)