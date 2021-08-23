package com.shop.tcd.Common

import com.shop.tcd.retro.RetrofitClient
import com.shop.tcd.retro.RetrofitServices

object Common {
    private val BASE_URL = "http://192.168.88.58/TSD/hs/TSD/"
    val retrofitService: RetrofitServices
        get() = RetrofitClient.getClient(BASE_URL).create(RetrofitServices::class.java)
}