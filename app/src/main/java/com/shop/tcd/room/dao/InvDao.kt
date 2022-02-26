package com.shop.tcd.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.shop.tcd.model.InvItem
import io.reactivex.rxjava3.core.Single

@Dao
interface InvDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(invItem: InvItem)

    @Query("select * from inventory order by uid desc")
    fun selectAll(): LiveData<List<InvItem>>

    @Query("select * from inventory order by uid desc")
    fun selectAllSingle(): Single<List<InvItem>>

    @Query("select * from inventory order by uid desc")
    suspend fun selectAllSuspend(): List<InvItem>

    @Query("delete from inventory where uid= :uid")
    suspend fun deleteInv(uid: Int)

    @Query("DELETE FROM inventory")
    suspend fun deleteAll()

    @Query("SELECT code,name,plu,barcode,count(barcode) as cnt, sum(cast(replace(quantity,\',\',\'.\') as float)) as quantity FROM inventory group by barcode")
    fun selectSumGroupByBarcode(): LiveData<List<InvItem>>

    @Query("update inventory set quantity = :newQuantity where uid = :uid")
    fun updateInventoryQuantity(uid: Int, newQuantity: String)
}