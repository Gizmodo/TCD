package com.shop.tcd.room.dao

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

    @Query("select * from inventory order by uid")
    fun selectAll(): LiveData<List<InvItem>>

    @Query("delete from inventory where uid= :uid")
    suspend fun deleteInv(uid: Int)

    @Query("DELETE FROM inventory")
    suspend fun deleteAll()
}