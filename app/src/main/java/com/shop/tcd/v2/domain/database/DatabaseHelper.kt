package com.shop.tcd.v2.domain.database

import com.shop.tcd.model.InvItem

interface DatabaseHelper {
    suspend fun getInventarisationItems(): List<InvItem>
    suspend fun updateInventoryQuantity(uid: Int, newQuantity:String )
}