package com.shop.tcd.data.user

import com.google.gson.annotations.SerializedName

data class UserModel(
    @SerializedName("Код")
    val code: String,
    @SerializedName("Наименование")
    val name: String,
    @SerializedName("Пароль")
    val password: String,
)