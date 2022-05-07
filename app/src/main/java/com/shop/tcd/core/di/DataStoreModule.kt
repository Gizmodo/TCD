package com.shop.tcd.core.di

import com.shop.tcd.data.local.DataStoreRepository
import com.shop.tcd.data.local.IDataStoreRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DataStoreModule {
    @Provides
    @Singleton
    fun provideDataStoreRepository(dataStoreRepositoryImpl: DataStoreRepository): IDataStoreRepository =
        dataStoreRepositoryImpl
}