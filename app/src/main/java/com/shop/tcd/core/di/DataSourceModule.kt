package com.shop.tcd.core.di

import com.shop.tcd.domain.database.DatabaseHelper
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DataSourceModule {
    @Provides
    @Singleton
    fun provideDataStoreRepository(ds: DatabaseHelperImpl): DatabaseHelper = ds
}