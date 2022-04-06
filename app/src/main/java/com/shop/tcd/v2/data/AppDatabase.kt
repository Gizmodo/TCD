package com.shop.tcd.v2.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.shop.tcd.model.InvItem
import com.shop.tcd.v2.data.dao.InventoryDao
import com.shop.tcd.v2.data.dao.NomenclatureDao
import com.shop.tcd.v2.data.nomenclature.NomenclatureItem

@Database(
    entities = [
        NomenclatureItem::class,
        InvItem::class
    ], version = 4, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun nomDao(): NomenclatureDao
    abstract fun invDao(): InventoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room
                    .databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
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