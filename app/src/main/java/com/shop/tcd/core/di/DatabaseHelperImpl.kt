package com.shop.tcd.core.di

import com.shop.tcd.data.AppDatabase
import com.shop.tcd.data.inventory.InvItem
import com.shop.tcd.domain.database.DatabaseHelper

// TODO: Refactor
class DatabaseHelperImpl(private val appDatabase: AppDatabase) : DatabaseHelper {
    override suspend fun getInventarisationItems(): List<InvItem> {
        return appDatabase.invDao().selectAllSuspend()
    }

    override suspend fun updateInventoryQuantity(uid: Int, newQuantity: String) {
        return appDatabase.invDao().updateInventoryQuantity(uid, newQuantity)
    }
}