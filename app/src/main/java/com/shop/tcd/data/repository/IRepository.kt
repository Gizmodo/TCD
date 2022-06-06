package com.shop.tcd.data.repository

import com.shop.tcd.data.dto.inventory.InvItem
import com.shop.tcd.data.dto.nomenclature.NomenclatureItem

interface IRepository {
    suspend fun getAll(): List<NomenclatureItem>
    suspend fun getInventarisationItems(): List<InvItem>
    suspend fun getNomenclatureBySearch(search: String): List<NomenclatureItem>
    suspend fun updateInventoryQuantity(uid: Int, newQuantity: String)
    suspend fun selectAllSuspend(): List<InvItem>
    suspend fun deleteAllNomenclature()
    suspend fun insertNomenclature(nomenclatureList: List<NomenclatureItem>)
    suspend fun deleteAllInventory()
    suspend fun loadInventoryGrouped(): List<InvItem>
    fun selectInventoryItemByCode(code: String): String?
    fun selectNomenclatureItemByCode(code: String): NomenclatureItem?
    fun selectInventoryItemByPLUCode(plu: String): String?
    fun selectNomenclatureItemByPLUCode(plu: String): NomenclatureItem?
    fun selectInventoryItemByBarcode(barcode: String): String?
    fun selectNomenclatureItemByBarcode(barcode: String): NomenclatureItem?
    suspend fun insertInventory(invItem: InvItem)
    suspend fun getItemForDetail(code: String, barcode: String): NomenclatureItem?
}
