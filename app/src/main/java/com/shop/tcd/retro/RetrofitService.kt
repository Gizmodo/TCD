package com.shop.tcd.retro

import com.shop.tcd.model.Groups
import com.shop.tcd.model.Nomenclature
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET

interface RetrofitService {
    @GET("getitemlist")
    fun getAllGoods(): Call<Nomenclature>

    @GET("getgrouplist")
    fun getAllGroups(): Call<Groups>

    companion object {
        var retrofitService: RetrofitService? = null

        fun getInstance(): RetrofitService {
            if (retrofitService == null) {
                var retrofit = Retrofit.Builder()
                    .baseUrl("http://10.0.2.2/TSD/hs/TSD/")
//                    .baseUrl("http://192.168.88.58/TSD/hs/TSD/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                retrofitService = retrofit.create(RetrofitService::class.java)
            }
            return retrofitService!!
        }
    }
}
