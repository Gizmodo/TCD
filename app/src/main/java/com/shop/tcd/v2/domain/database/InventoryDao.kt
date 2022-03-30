package com.shop.tcd.v2.domain.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shop.tcd.model.InvItem

@Dao
interface InventoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(invItem: InvItem)

    @Query("select * from inventory order by uid desc")
    suspend fun selectAllSuspend(): List<InvItem>

    @Query("DELETE FROM inventory")
    suspend fun deleteAll()

    @Query("SELECT uid, code,name,plu,barcode,count(barcode) as cnt, sum(cast(replace(quantity,\',\',\'.\') as float)) as quantity FROM inventory group by barcode order by uid desc")
    suspend fun loadInventoryGrouped(): List<InvItem>

    @Query("update inventory set quantity = :newQuantity where uid = :uid")
    fun updateInventoryQuantity(uid: Int, newQuantity: String)
}