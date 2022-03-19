package com.shop.tcd.v2.core.di

import com.bugsnag.android.okhttp.BugsnagOkHttpPlugin
import com.google.gson.GsonBuilder
import com.shop.tcd.v2.core.utils.Common
import com.shop.tcd.v2.core.utils.Common.OK_HTTP_TIMEOUT
import com.shop.tcd.v2.domain.rest.SettingsApi
import com.shop.tcd.v2.domain.rest.ShopApi
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
        Timber.d("Создан provideSettingsApi")
        return retrofit.create(SettingsApi::class.java)
    }

    @Singleton
    @Provides
    fun provideShopApi(retrofit: Retrofit): ShopApi {
        Timber.d("Создан provideShopApi")
        return retrofit.create(ShopApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        Timber.d("Создан provideOkHttpClient")
        return OkHttpClient.Builder()
            .eventListener(BugsnagOkHttpPlugin())
            .connectTimeout(OK_HTTP_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(OK_HTTP_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(OK_HTTP_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    fun provideRetrofitInterface(okHttpClient: OkHttpClient): Retrofit {
        Timber.d("Создан provideRetrofitInterface")
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
