package com.shop.tcd.v2.domain.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shop.tcd.v2.data.nomenclature.NomenclatureItem

@Dao
interface NomenclatureDao {
    // TODO: убрать лимит и сделать через страницы
    @Query("select * from nomenclature limit 5000")
    suspend fun getAll(): List<NomenclatureItem>

    /* Вставка всей номенклатуры */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNomenclature(nomenclatureList: List<NomenclatureItem>)

    /* Удалить все записи номенклатуры */
    @Query("DELETE FROM nomenclature")
    suspend fun deleteAll()

    /* Поиск товара по штрихкоду */
    @Query("select * from nomenclature where barcode = :barcode limit 1")
    fun getByBarcode(barcode: String): LiveData<NomenclatureItem>

    /* Поиск товара по коду */
    @Query("select * from nomenclature where code = :code limit 1")
    fun getByCode(code: String): LiveData<NomenclatureItem>

    /* Поиск товара по PLU */
    @Query("select * from nomenclature where plu = :plu limit 1")
    fun getByPLU(plu: String): LiveData<NomenclatureItem>

    @Query("select * from nomenclature where code LIKE '%' || :search || '%' or barcode LIKE '%' || :search || '%' or name LIKE '%' || :search || '%'")
    suspend fun getNomenclatureBySearch(search: String): List<NomenclatureItem>
}