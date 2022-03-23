package com.shop.tcd.v2.core.di

import com.shop.tcd.room.database.DatabaseHelperImpl
import com.shop.tcd.v2.domain.database.DatabaseHelper
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DataSourceModule {
    @Provides
    @Singleton
    fun provideDataStoreRepository(ds: DatabaseHelperImpl): DatabaseHelper = ds
}