package com.shop.tcd.di

import com.shop.tcd.room.database.DatabaseHelper
import com.shop.tcd.room.database.DatabaseHelperImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DataSourceModule {
    @Provides
    @Singleton
    fun provideDataStoreRepository(ds: DatabaseHelperImpl): DatabaseHelper = ds
}