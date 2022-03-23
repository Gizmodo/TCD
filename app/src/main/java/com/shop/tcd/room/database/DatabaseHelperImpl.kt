package com.shop.tcd.room.database

import com.shop.tcd.model.InvItem
import com.shop.tcd.v2.domain.database.DatabaseHelper

class DatabaseHelperImpl(private val appDatabase: TCDRoomDatabase) : DatabaseHelper {
    override suspend fun getInventarisationItems(): List<InvItem> {
        return appDatabase.invDao().selectAllSuspend()
    }

    override suspend fun updateInventoryQuantity(uid: Int, newQuantity: String) {
        return appDatabase.invDao().updateInventoryQuantity(uid, newQuantity)
    }
}