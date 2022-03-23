package com.shop.tcd.repository.main

import com.google.gson.GsonBuilder
import com.shop.tcd.model.post.Payload
import com.shop.tcd.v2.core.utils.Common
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import timber.log.Timber
import java.util.concurrent.TimeUnit

interface RetrofitServiceMain {
    /**
     * Отправить ответ по инвентаризации
     **/
    @POST("receiveddocument")
    @Headers("Content-Type:application/json")
    fun postInventory(
        @Query("filter") filter: String = "",
        @Body document: Payload,
    ): Call<String>

    companion object {
        private var retroService: RetrofitServiceMain? = null
        fun getInstance(): RetrofitServiceMain {
            val logging = HttpLoggingInterceptor { message -> Timber.d(message) }
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC)

            val gson = GsonBuilder().setLenient().create()

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .build()

            val retro = Retrofit.Builder()
                .baseUrl(Common.BASE_SHOP_URL)
                .client(client)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            retroService = retro.create(RetrofitServiceMain::class.java)
            Timber.d("Создан retrotest c baseUrl = ${Common.BASE_SHOP_URL}")

            return retroService!!
        }
    }
}