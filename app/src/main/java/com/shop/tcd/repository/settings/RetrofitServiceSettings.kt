package com.shop.tcd.repository.settings

import com.google.gson.GsonBuilder
import com.shop.tcd.model.newsettigs.PrintersList
import com.shop.tcd.utils.Common
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import timber.log.Timber
import java.util.concurrent.TimeUnit

interface RetrofitServiceSettings {

    /**
     * Загрузить файл с настройками магазинов и пользователей
     **/
    @GET("SettingsForTSD.xml")
    fun getSettings(): Call<String>

    @GET("Tech/hs/tsd/printers/get")
    fun getPrinters(): Observable<PrintersList>

    companion object {
        private var retrofitServiceSettings: RetrofitServiceSettings? = null

        fun getInstance(): RetrofitServiceSettings {
            Timber.d("RetrofitServiceSettings.getInstance Called")
            if (retrofitServiceSettings == null) {
                val logging = HttpLoggingInterceptor { message -> Timber.i(message) }
                logging.setLevel(HttpLoggingInterceptor.Level.BODY)

                val gson = GsonBuilder().setLenient().create()

                val client = OkHttpClient.Builder()
//                    .addInterceptor(logging)
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(1, TimeUnit.MINUTES)
                    .build()

                val retrofit = Retrofit.Builder()
//                    .baseUrl("http://10.0.2.2/") // тестовый для XML локальный
                    .baseUrl(Common.BASE_URL)
//                  .baseUrl("http://10.10.10.220/") // тестовый для XML локальный
//                  .baseUrl("http://10.0.2.2/TSD/hs/TSD/") // боевой локальный для JSON
                    .client(client)
//                    .baseUrl("http://192.168.88.33/TSD/hs/TSD/") // боевой адрес для Wi-Fi
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))

                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
                retrofitServiceSettings = retrofit.create(RetrofitServiceSettings::class.java)
                Timber.d("Создан retrofitServiceSettings c baseUrl = ${Common.BASE_URL}")

            }
            return retrofitServiceSettings!!
        }
    }
}
