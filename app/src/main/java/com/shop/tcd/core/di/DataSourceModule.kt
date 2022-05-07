package com.shop.tcd.core.di

import com.shop.tcd.data.repository.IRepository
import com.shop.tcd.data.repository.Repository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DataSourceModule {
    @Provides
    @Singleton
    fun provideDatabaseHelper(repository: Repository): IRepository = repository
}