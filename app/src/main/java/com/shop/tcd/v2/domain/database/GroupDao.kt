package com.shop.tcd.v2.domain.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shop.tcd.model.Group
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groupsList: List<Group>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: Group)

    @Query("SELECT * FROM `Group` WHERE name like :search")
    fun getGroup(search: String): List<Group>

    @Query("delete from `Group`")
    suspend fun deleteAllGroups()

    @Query("SELECT * FROM `Group` ORDER BY code ASC")
    fun getAlphabetizedWords(): Flow<List<Group>>

    //Получить список всех групп товаров
    @Query("SELECT * FROM `Group`")
    fun getAllGroups(): List<Group>

}