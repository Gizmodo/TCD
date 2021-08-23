package com.shop.tcd.retro

import com.shop.tcd.model.Groups
import retrofit2.Call
import retrofit2.http.*

interface RetrofitServices {
    @GET("getgrouplist")
    fun getMovieList(): Call<MutableList<Groups>>
}