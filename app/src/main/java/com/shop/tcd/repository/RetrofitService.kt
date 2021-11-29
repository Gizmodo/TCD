package com.shop.tcd.repository

import com.google.gson.GsonBuilder
import com.shop.tcd.model.Groups
import com.shop.tcd.model.Nomenclature
import com.shop.tcd.model.post.Payload
import com.shop.tcd.model.settings.Settings
import com.shop.tcd.utils.Common
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface RetrofitService {

    /**
     *Загрузить весь список товаров
     *
     **/
    @GET("TSD/hs/TSD/getitemlist")
    fun getAllItems(): Call<Nomenclature>

    /**
     *Загрузить группы товаров
     *
     **/
    @GET("TSD/hs/TSD/getgrouplist")
    fun getAllGroups(): Call<Groups>

    /**
     *Загрузить товары по остаткам
     *
     **/
    @GET("TSD/hs/TSD/getiteminstock")
    fun getRemainders(): Call<Nomenclature>

    /**
     *Загрузить товары за период
     *"01.09.2021 0:00:00,30.09.2021 23:59:59"
     * 12.07.2021 23:59:58
     * 11.07.2021 17:31:48
     * 10.07.2021 23:59:58
     * 01.10.2019 0:00:00
     **/
    @GET("TSD/hs/TSD/getturnoverfortheperiod/")
    fun getPeriod(@Query("filter") filter: String): Call<Nomenclature>

    /**
     * Загрузить товары по выбранным группам
     * filter=10000, 20000, ....
     **/
    @GET("TSD/hs/TSD/getgrouplist/")
    fun getByGroup(@Query("filter") filter: String): Call<Nomenclature>

    /**
     * Отправить ответ по инвентаризации
     **/
    @POST("TSD/hs/TSD/receiveddocument")
    @Headers("Content-Type:application/json")
    fun postInventory(
        @Query("filter") filter: String = "",
        @Body document: Payload,
    ): Call<String>

    /**
     * Загрузить файл с настройками магазинов и пользователей
     **/
    @GET("SettingsForTSD.json")
    fun getSettings(): Call<Settings>

    companion object {
        private var retrofitService: RetrofitService? = null

        fun getInstance(): RetrofitService {
            if (retrofitService == null) {

                val client = OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(1, TimeUnit.MINUTES)
                    .build()

                val gson = GsonBuilder()
                    .setLenient()
                    .create()

                val retrofit = Retrofit.Builder()
//                    .baseUrl("http://10.0.2.2/") // тестовый для XML локальный
                    .baseUrl(Common.BASE_URL)
//                  .baseUrl("http://10.10.10.220/") // тестовый для XML локальный
//                  .baseUrl("http://10.0.2.2/TSD/hs/TSD/") // боевой локальный для JSON
                    .client(client)
//                    .baseUrl("http://192.168.88.33/TSD/hs/TSD/") // боевой адрес для Wi-Fi
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
                retrofitService = retrofit.create(RetrofitService::class.java)
            }
            return retrofitService!!
        }
    }
}
