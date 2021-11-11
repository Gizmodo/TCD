package com.shop.tcd.Common

import android.content.Context
import com.shop.tcd.model.InvItem
import com.shop.tcd.model.NomenclatureItem
import com.shop.tcd.room.dao.InvDao
import com.shop.tcd.room.dao.NomenclatureDao
import com.shop.tcd.room.database.TCDRoomDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object Common {
    private val BASE_URL = "http://192.168.88.58/TSD/hs/TSD/"

    suspend fun saveNomenclatureList(list: List<NomenclatureItem>, context: Context) {
        val nomenclatureDao: NomenclatureDao
        val databaseTCD: TCDRoomDatabase = TCDRoomDatabase.getDatabase(context)
        nomenclatureDao = databaseTCD.nomDao()
        GlobalScope.launch {
            nomenclatureDao.deleteAll()
            nomenclatureDao.insertNomenclature(list)
        }
    }

    suspend fun insertInv(item: InvItem, context: Context) {
        val invDao: InvDao
        val databaseTCD: TCDRoomDatabase = TCDRoomDatabase.getDatabase(context)
        invDao = databaseTCD.invDao()
        GlobalScope.launch {
            invDao.insert(item)
        }
    }

    fun deleteAllInv(context: Context) {
        val invDao: InvDao
        val database: TCDRoomDatabase = TCDRoomDatabase.getDatabase(context)
        invDao = database.invDao()
        GlobalScope.launch {
            invDao.deleteAll()
        }
    }
}