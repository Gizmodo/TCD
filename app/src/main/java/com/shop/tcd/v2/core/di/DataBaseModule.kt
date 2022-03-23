package com.shop.tcd.v2.core.di

import android.content.Context
import com.shop.tcd.room.database.TCDRoomDatabase
import com.shop.tcd.v2.domain.database.GroupDao
import com.shop.tcd.v2.domain.database.InvDao
import com.shop.tcd.v2.domain.database.NomenclatureDao
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Singleton


@Module
class DataBaseModule @Inject constructor(private val context: Context) {

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
        return TCDRoomDatabase.getDatabase(context)
    }
}