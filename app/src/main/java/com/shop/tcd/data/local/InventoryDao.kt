package com.shop.tcd.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shop.tcd.data.dto.inventory.InvItem

@Dao
interface InventoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(invItem: InvItem)

    @Query("select * from inventory order by uid desc")
    suspend fun selectAllSuspend(): List<InvItem>

    @Query("DELETE FROM inventory")
    suspend fun deleteAll()

    @Query("SELECT uid, code,name,plu,barcode,count(barcode) as cnt, sum(cast(replace(quantity,\',\',\'.\') as float)) as quantity FROM inventory group by code, plu order by uid desc")
    suspend fun loadInventoryGrouped(): List<InvItem>

    @Query("update inventory set quantity = :newQuantity where uid = :uid")
    suspend fun updateInventoryQuantity(uid: Int, newQuantity: String)

    @Query("select sum(cast(replace(quantity,',','.') as float)) as cnt from inventory where code = :code group by code, barcode limit 1")
    fun selectInventoryItemByCode(code: String): String?

    @Query("select sum(cast(replace(quantity,',','.') as float)) as cnt from inventory where barcode = :barcode group by code, barcode limit 1")
    fun selectInventoryItemByBarcode(barcode: String): String?

    @Query("select sum(cast(replace(quantity,',','.') as float)) as cnt from inventory where plu = :plu group by code, barcode limit 1")
    fun selectInventoryItemByPLUCode(plu: String): String?
}
