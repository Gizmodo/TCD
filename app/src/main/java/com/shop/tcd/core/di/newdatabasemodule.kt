package com.shop.tcd.core.di

import android.app.Application
import androidx.room.Room
import com.shop.tcd.data.local.AppDatabase
import com.shop.tcd.data.local.InventoryDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
@Module
class newdatabasemodule(application: Application) {

    /*private val taskDataBase: AppDatabase =
        Room.databaseBuilder(application.applicationContext, AppDatabase::class.java, "task")
            .fallbackToDestructiveMigration().allowMainThreadQueries().build()

    @Singleton
    @Provides
    internal fun providesRoomDatabase(): AppDatabase {
        return taskDataBase
    }

    @Singleton
    @Provides
    internal fun providesTaskDao(taskDataBase: AppDatabase): InventoryDao {
        return taskDataBase.invDao()
    }*/


}