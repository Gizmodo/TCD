package com.shop.tcd.retro

import com.shop.tcd.model.Groups
import com.shop.tcd.model.Nomenclature
import okhttp3.OkHttp
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface RetrofitService {

    /**
     *Загрузить весь список товаров
     *
     **/
    @GET("getitemlist")
    fun getAllItems(): Call<Nomenclature>

    /**
     *Загрузить группы товаров
     *
     **/
    @GET("getgrouplist")
    fun getAllGroups(): Call<Groups>

    /**
     *Загрузить товары по остаткам
     *
     **/
    @GET("getiteminstock")
    fun getRemainders(): Call<Nomenclature>

    /**
     *Загрузить товары за период
     *"01.09.2021 0:00:00,30.09.2021 23:59:59"
     * 12.07.2021 23:59:58
     * 11.07.2021 17:31:48
     * 10.07.2021 23:59:58
     * 01.10.2019 0:00:00
     **/
    @GET("getturnoverfortheperiod/")
    fun getPeriod(@Query("filter") filter: String): Call<Nomenclature>

    companion object {
        var retrofitService: RetrofitService? = null

        fun getInstance(): RetrofitService {
            if (retrofitService == null) {
                val httpLoggingInterceptor = HttpLoggingInterceptor()
                httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY


                val okhttpclient = OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(1, TimeUnit.MINUTES)
                    //  .addInterceptor(httpLoggingInterceptor)
                    .build()

                var retrofit = Retrofit.Builder()
                    .baseUrl("http://10.0.2.2/TSD/hs/TSD/")
                    .client(okhttpclient)
//                    .baseUrl("http://192.168.88.58/TSD/hs/TSD/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                retrofitService = retrofit.create(RetrofitService::class.java)
            }
            return retrofitService!!
        }
    }
}
