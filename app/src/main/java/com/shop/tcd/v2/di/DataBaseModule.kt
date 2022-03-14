package com.shop.tcd.v2.di

import android.app.Application
import com.shop.tcd.room.dao.GroupDao
import com.shop.tcd.room.dao.InvDao
import com.shop.tcd.room.dao.NomenclatureDao
import com.shop.tcd.room.database.TCDRoomDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataBaseModule(private val application: Application) {

    @Singleton
    @Provides
    fun provideInventoryDao(database: TCDRoomDatabase): InvDao {
        return database.invDao()
    }

    @Singleton
    @Provides
    fun provideGroupDao(database: TCDRoomDatabase): GroupDao {
        return database.wordDao()
    }

    @Singleton
    @Provides
    fun provideNomeclatureDao(database: TCDRoomDatabase): NomenclatureDao {
        return database.nomDao()
    }

    @Singleton
    @Provides
    fun provideDBInstance(): TCDRoomDatabase {
        return TCDRoomDatabase.getDatabase(application)
    }


}