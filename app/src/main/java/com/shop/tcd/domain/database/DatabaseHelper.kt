package com.shop.tcd.domain.database

import com.shop.tcd.data.inventory.InvItem

interface DatabaseHelper {
    suspend fun getInventarisationItems(): List<InvItem>
    suspend fun updateInventoryQuantity(uid: Int, newQuantity: String)
}