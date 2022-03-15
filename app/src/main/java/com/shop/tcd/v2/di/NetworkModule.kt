package com.shop.tcd.v2.di

import com.google.gson.GsonBuilder
import com.shop.tcd.repository.network.main.MainApi
import com.shop.tcd.repository.network.settings.SettingsApi
import com.shop.tcd.utils.Common
import com.shop.tcd.utils.Common.OK_HTTP_TIMEOUT
import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
object NetworkModule {

    @Singleton
    @Provides
    fun provideSettingsApi(retrofit: Retrofit): SettingsApi {
        return retrofit.create(SettingsApi::class.java)
    }

    @Singleton
    @Provides
    fun provideMainApi(retrofit: Retrofit): MainApi {
        return retrofit.create(MainApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(OK_HTTP_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(OK_HTTP_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(OK_HTTP_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    fun provideRetrofitInterface(okHttpClient: OkHttpClient): Retrofit {
        Timber.d("Создан Retrofit")

        val logging = HttpLoggingInterceptor { message -> Timber.i(message) }
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        val gson = GsonBuilder().setLenient().create()
        return Retrofit.Builder()
            .baseUrl(Common.BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .client(okHttpClient)
            .build()
    }
}
