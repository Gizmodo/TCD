package com.shop.tcd.core.di

import com.bugsnag.android.okhttp.BugsnagOkHttpPlugin
import com.google.gson.GsonBuilder
import com.shop.tcd.core.utils.BasicAuthInterceptor
import com.shop.tcd.core.utils.Constants.Network.BASE_SHOP_URL
import com.shop.tcd.core.utils.Constants.Network.BASE_URL
import com.shop.tcd.core.utils.Constants.Network.BASE_URL_UPDATE_SERVER
import com.shop.tcd.core.utils.Constants.Network.OK_HTTP_SETTINGS_TIMEOUT
import com.shop.tcd.core.utils.Constants.Network.OK_HTTP_SHOP_TIMEOUT
import com.shop.tcd.core.utils.Constants.Network.OK_HTTP_UPDATE_TIMEOUT
import com.shop.tcd.data.remote.SettingsApi
import com.shop.tcd.data.remote.ShopApi
import com.shop.tcd.data.remote.UpdateApi
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
import javax.inject.Named
import javax.inject.Singleton

@Module
object NetworkModule {
    @Singleton
    @Provides
    fun provideSettingsApi(@Named("Settings") retrofit: Retrofit): SettingsApi {
        return retrofit.create(SettingsApi::class.java)
    }

    @Singleton
    @Provides
    fun provideShopApi(@Named("Shop") retrofit: Retrofit): ShopApi {
        return retrofit.create(ShopApi::class.java)
    }

    @Singleton
    @Provides
    fun providesUpdateApi(@Named("Update") retrofit: Retrofit): UpdateApi =
        retrofit.create(UpdateApi::class.java)

    @Provides
    @Named("Settings")
    fun provideOkHttpClientSettings(): OkHttpClient {
        val logging = HttpLoggingInterceptor { message -> Timber.i(message) }
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC)

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(BasicAuthInterceptor("tsd", "tsd159753"))
            .eventListener(BugsnagOkHttpPlugin())
            .connectTimeout(OK_HTTP_SETTINGS_TIMEOUT)
            .readTimeout(OK_HTTP_SETTINGS_TIMEOUT)
            .writeTimeout(OK_HTTP_SETTINGS_TIMEOUT)
            .build()
    }

    @Provides
    @Named("Shop")
    fun provideOkHttpClientShop(): OkHttpClient {
        val logging = HttpLoggingInterceptor { message -> Timber.i(message) }
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC)

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .eventListener(BugsnagOkHttpPlugin())
            .connectTimeout(OK_HTTP_SHOP_TIMEOUT)
            .readTimeout(OK_HTTP_SHOP_TIMEOUT)
            .writeTimeout(OK_HTTP_SHOP_TIMEOUT)
            .build()
    }

    @Provides
    @Named("Update")
    fun providesUpdateOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor { message -> Timber.i(message) }
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC)
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .eventListener(BugsnagOkHttpPlugin())
            .connectTimeout(OK_HTTP_UPDATE_TIMEOUT)
            .readTimeout(OK_HTTP_UPDATE_TIMEOUT)
            .writeTimeout(OK_HTTP_UPDATE_TIMEOUT)
            .build()
    }

    @Provides
    @Named("Settings")
    fun provideRetrofitInterface(@Named("Settings") okHttpClient: OkHttpClient): Retrofit {
        val gson = GsonBuilder().setLenient().create()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Named("Shop")
    fun provideRetrofitForShop(@Named("Shop") okHttpClient: OkHttpClient): Retrofit {
        val gson = GsonBuilder().setLenient().create()
        return Retrofit.Builder()
            .baseUrl(BASE_SHOP_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Named("Update")
    fun providesUpdateRetrofit(@Named("Update") okHttpClient: OkHttpClient): Retrofit {
        val gson = GsonBuilder().setLenient().create()
        return Retrofit.Builder()
            .baseUrl(BASE_URL_UPDATE_SERVER)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
    }
}
