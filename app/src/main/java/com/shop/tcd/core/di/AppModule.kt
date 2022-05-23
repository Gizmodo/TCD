package com.shop.tcd.core.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object AppModule {
    var mApplication: Application? = null

    fun AppModule(application: Application?) {
        mApplication = application
    }

    @Provides
    @Singleton
    fun providesApplication(): Application? {
        return mApplication
    }

    @Provides
    @Singleton
    fun providesApplicationContext(application: Application): Context = application
}