package com.shop.tcd.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shop.tcd.data.nomenclature.NomenclatureItem

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

    /* Поиск товара по коду */
    @Query("select * from nomenclature where code = :code limit 1")
    fun selectNomenclatureItemByCode(code: String): NomenclatureItem?

    /* Поиск товара по PLU */
    @Query("select * from nomenclature where plu = :plu limit 1")
    fun selectNomenclatureItemByPLUCode(plu: String): NomenclatureItem?

    /* Поиск товара по ШК */
    @Query("select * from nomenclature where barcode = :barcode limit 1")
    fun selectNomenclatureItemByBarcode(barcode: String): NomenclatureItem?

    @Query("select * from nomenclature where code LIKE '%' || :search || '%' or barcode LIKE '%' || :search || '%' or name LIKE '%' || :search || '%'")
    suspend fun getNomenclatureBySearch(search: String): List<NomenclatureItem>

    @Query("SELECT * from nomenclature where code = :code and barcode = :barcode limit 1")
    suspend fun getItemForDetail(code: String, barcode: String): NomenclatureItem?
}