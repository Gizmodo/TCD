package com.shop.tcd.core.di

import android.app.Application
import androidx.room.Room
import com.shop.tcd.data.local.AppDatabase
import com.shop.tcd.data.local.InventoryDao
import com.shop.tcd.data.local.NomenclatureDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataBaseModule(application: Application) {
    private var libraryApplication = application
    private lateinit var libraryDatabase: AppDatabase

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
    fun providesRoomDatabase(): AppDatabase {
        libraryDatabase =
            Room.databaseBuilder(libraryApplication, AppDatabase::class.java, "tcd_database.db")
                .fallbackToDestructiveMigration()
                .build()
        return libraryDatabase
    }
}