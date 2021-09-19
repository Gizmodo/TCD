package com.shop.tcd.room.dao

import androidx.lifecycle.LiveData
import com.shop.tcd.model.NomenclatureItem
import kotlinx.coroutines.flow.Flow
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NomenclatureDao {
    @Query("select * from nomenclature limit 5000")
    suspend fun getAll(): List<NomenclatureItem>

    @Query("select * from nomenclature")
    fun getAllLiveData(): LiveData<List<NomenclatureItem>>

    @Query("select * from nomenclature ORDER BY uid ASC")
    fun getAllFlow(): Flow<List<NomenclatureItem>>

    //Вставка всей номенклатуры
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNomenclature(nomenclatureList: List<NomenclatureItem>)

    //Удалить все записи номенклатуры
    @Query("DELETE FROM nomenclature")
    suspend fun deleteAll()

    //Поиск товара по штрихкоду
    @Query("select * from nomenclature where barcode = :barcode")
    suspend fun getByBarcode(barcode: String): List<NomenclatureItem>

    //Количество записей
    @Query("SELECT COUNT(*) FROM nomenclature")
    suspend fun count(): Int
}