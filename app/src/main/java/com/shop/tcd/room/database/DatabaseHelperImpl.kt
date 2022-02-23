package com.shop.tcd.room.database

import com.shop.tcd.model.InvItem

class DatabaseHelperImpl(private val appDatabase: TCDRoomDatabase) : DatabaseHelper {
    override suspend fun getInventarisationItems(): List<InvItem> {
        return appDatabase.invDao().selectAllSuspend()
    }
}