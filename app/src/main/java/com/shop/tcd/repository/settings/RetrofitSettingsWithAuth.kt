package com.shop.tcd.repository.settings

import com.google.gson.GsonBuilder
import com.shop.tcd.model.newsettigs.UserListItem
import com.shop.tcd.utils.BasicAuthInterceptor
import com.shop.tcd.utils.Common
import io.reactivex.rxjava3.core.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import timber.log.Timber
import java.util.concurrent.TimeUnit

interface RetrofitSettingsWithAuth {
    @GET("Tech/hs/tsd/users/get")
    fun getUsers(): Observable<List<UserListItem>>

    companion object {
        private var retrofitSettingsWithAuth: RetrofitSettingsWithAuth? = null

        fun getInstance(): RetrofitSettingsWithAuth {
            if (retrofitSettingsWithAuth == null) {
                val logging = HttpLoggingInterceptor { message -> Timber.i(message) }
                logging.setLevel(HttpLoggingInterceptor.Level.HEADERS)

                val gson = GsonBuilder().setLenient().create()

                val client = OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(BasicAuthInterceptor("tsd", "tsd159753"))
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(1, TimeUnit.MINUTES)
                    .build()

                val retrofit = Retrofit.Builder()
                    .baseUrl(Common.BASE_URL)
                    .client(client)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .build()
                retrofitSettingsWithAuth = retrofit.create(RetrofitSettingsWithAuth::class.java)
            }
            return retrofitSettingsWithAuth!!
        }
    }
}