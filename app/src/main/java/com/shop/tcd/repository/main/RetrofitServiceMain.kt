package com.shop.tcd.repository.main

import com.google.gson.GsonBuilder
import com.shop.tcd.model.Groups
import com.shop.tcd.model.Nomenclature
import com.shop.tcd.model.post.Payload
import com.shop.tcd.utils.Common
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*
import timber.log.Timber
import java.util.concurrent.TimeUnit

interface RetrofitServiceMain {
    /**
     *Загрузить группы товаров
     *
     **/
//    @GET("TSD/hs/TSD/getgrouplist")
    @GET("getgrouplist")
    fun getAllGroups(): Call<Groups>

    /**
     * Загрузить товары по выбранным группам
     * filter=10000, 20000, ....
     **/
    @GET("getgrouplist")
    fun getByGroup(@Query("filter") filter: String): Call<Nomenclature>

    /**
     *Загрузить весь список товаров
     *
     **/
    @GET("getitemlist")
    fun getAllItems(): Call<Nomenclature>

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
    @GET("getturnoverfortheperiod")
    fun getPeriod(@Query("filter") filter: String): Call<Nomenclature>

    /**
     * Отправить ответ по инвентаризации
     **/
    @POST("receiveddocument")
    @Headers("Content-Type:application/json")
    fun postInventory(
        @Query("filter") filter: String = "",
        @Body document: Payload,
    ): Call<String>

    /**
     * **** TEST ***
    @GET //("TSD/hs/TSD/getgrouplist")
    fun getAllGroupsURL(@Url url: String): Call<Groups>
     **/
    companion object {
        private var retroService: RetrofitServiceMain? = null
        fun getInstance(): RetrofitServiceMain {
            val logging = HttpLoggingInterceptor { message -> Timber.d(message) }
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC)

            val gson = GsonBuilder().setLenient().create()

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
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