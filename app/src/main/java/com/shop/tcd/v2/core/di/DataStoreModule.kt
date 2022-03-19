package com.shop.tcd.v2.core.di

import com.shop.tcd.v2.datastore.DataStoreRepository
import com.shop.tcd.v2.datastore.DataStoreRepositoryImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DataStoreModule {
    @Provides
    @Singleton
    fun provideDataStoreRepository(dataStoreRepositoryImpl: DataStoreRepositoryImpl): DataStoreRepository =
        dataStoreRepositoryImpl
}