package com.shop.tcd.utils

import android.content.Context
import com.shop.tcd.model.InvItem
import com.shop.tcd.model.NomenclatureItem
import com.shop.tcd.model.settings.GroupUser
import com.shop.tcd.model.settings.Shop
import com.shop.tcd.room.dao.InvDao
import com.shop.tcd.room.dao.NomenclatureDao
import com.shop.tcd.room.database.TCDRoomDatabase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object Common {
    /*val BASE_URL_ = "http://192.168.88.58/TSD/hs/TSD/"
    var BASE_URL_HOME = "http://10.0.2.2/"
*/
    /**
     * Постоянный адрес с расположением файла настроек
     */
    var BASE_URL = "http://192.168.0.154/"

    /**
     * Изменяемый адрес, который указывает на выбранный магазин (сервер 1С)
     */
    var BASE_SHOP_URL = ""

    var shopsArray: ArrayList<Shop> = arrayListOf()
    var usersArray: ArrayList<GroupUser> = arrayListOf()

    /**
     * Хранение выбранного магазина
     */
    lateinit var selectedShop: Shop

    /**
     * Хранение выбранного пользователя и его позиции
     */
    lateinit var selectedUser: GroupUser
    var selectedUserPosition: Int = -1

    fun isInit(): Boolean {
        return this::selectedShop.isInitialized
    }

    @DelicateCoroutinesApi
    suspend fun saveNomenclatureList(list: List<NomenclatureItem>, context: Context) {
        val nomenclatureDao: NomenclatureDao
        val databaseTCD: TCDRoomDatabase = TCDRoomDatabase.getDatabase(context)
        nomenclatureDao = databaseTCD.nomDao()
        GlobalScope.launch {
            nomenclatureDao.deleteAll()
            nomenclatureDao.insertNomenclature(list)
        }
    }

    @DelicateCoroutinesApi
    suspend fun insertInv(item: InvItem, context: Context) {
        val invDao: InvDao
        val databaseTCD: TCDRoomDatabase = TCDRoomDatabase.getDatabase(context)
        invDao = databaseTCD.invDao()
        GlobalScope.launch {
            invDao.insert(item)
        }
    }

    @DelicateCoroutinesApi
    fun deleteAllInv(context: Context) {
        val invDao: InvDao
        val database: TCDRoomDatabase = TCDRoomDatabase.getDatabase(context)
        invDao = database.invDao()
        GlobalScope.launch {
            invDao.deleteAll()
        }
    }
}