package com.shop.tcd.v2.domain.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shop.tcd.model.InvItem

@Dao
interface InvDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(invItem: InvItem)

    @Query("select * from inventory order by uid desc")
    fun selectAll(): LiveData<List<InvItem>>

    @Query("select * from inventory order by uid desc")
    suspend fun selectAllSuspend(): List<InvItem>

    @Query("delete from inventory where uid= :uid")
    suspend fun deleteInv(uid: Int)

    @Query("DELETE FROM inventory")
    suspend fun deleteAll()

    @Query("SELECT code,name,plu,barcode,count(barcode) as cnt, sum(cast(replace(quantity,\',\',\'.\') as float)) as quantity FROM inventory group by barcode")
    fun selectSumGroupByBarcode(): LiveData<List<InvItem>>

    @Query("SELECT code,name,plu,barcode,count(barcode) as cnt, sum(cast(replace(quantity,\',\',\'.\') as float)) as quantity FROM inventory group by barcode")
    suspend fun loadInventoryGrouped(): List<InvItem>

    @Query("update inventory set quantity = :newQuantity where uid = :uid")
    fun updateInventoryQuantity(uid: Int, newQuantity: String)
}