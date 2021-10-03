package com.shop.tcd.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.shop.tcd.model.Group
import com.shop.tcd.model.InvItem
import com.shop.tcd.model.NomenclatureItem
import com.shop.tcd.room.dao.GroupDao
import com.shop.tcd.room.dao.InvDao
import com.shop.tcd.room.dao.NomenclatureDao

@Database(entities = [
    Group::class,
    NomenclatureItem::class,
    InvItem::class
], version = 2, exportSchema = false)
abstract class TCDRoomDatabase : RoomDatabase() {
    abstract fun wordDao(): GroupDao
    abstract fun nomDao(): NomenclatureDao
    abstract fun invDao(): InvDao

    companion object {
        @Volatile
        private var INSTANCE: TCDRoomDatabase? = null

        fun getDatabase(context: Context): TCDRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room
                    .databaseBuilder(
                        context.applicationContext,
                        TCDRoomDatabase::class.java,
                        "tcd_database.db"
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

}