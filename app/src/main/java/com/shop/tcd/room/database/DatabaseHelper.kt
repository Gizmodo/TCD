package com.shop.tcd.room.database

import com.shop.tcd.model.InvItem

interface DatabaseHelper {
    suspend fun getInventarisationItems(): List<InvItem>
    suspend fun updateInventoryQuantity(uid: Int, newQuantity:String )
}