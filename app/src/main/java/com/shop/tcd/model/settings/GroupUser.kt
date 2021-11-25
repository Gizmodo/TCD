package com.shop.tcd.model.settings


import com.google.gson.annotations.SerializedName

data class GroupUser(
    @SerializedName("userLogin")
    val userLogin: String,
    @SerializedName("userPassword")
    val userPassword: String,
)