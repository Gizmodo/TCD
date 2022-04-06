package com.shop.tcd.v2.core.di

import android.content.Context
import com.shop.tcd.v2.data.AppDatabase
import com.shop.tcd.v2.data.dao.InventoryDao
import com.shop.tcd.v2.data.dao.NomenclatureDao
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Singleton

@Module
class DataBaseModule @Inject constructor(private val context: Context) {

    @Singleton
    @Provides
    fun provideInventoryDao(database: AppDatabase): InventoryDao {
        return database.invDao()
    }

    @Singleton
    @Provides
    fun provideNomeclatureDao(database: AppDatabase): NomenclatureDao {
        return database.nomDao()
    }

    @Singleton
    @Provides
    fun provideDBInstance(): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
}