package com.shop.tcd.data.repository

import com.shop.tcd.data.dto.inventory.InvItem
import com.shop.tcd.data.dto.nomenclature.NomenclatureItem
import com.shop.tcd.data.local.InventoryDao
import com.shop.tcd.data.local.NomenclatureDao
import javax.inject.Inject

class Repository @Inject constructor(
    private val inventoryDao: InventoryDao,
    private val nomenclatureDao: NomenclatureDao,
) : IRepository {
    override suspend fun getAll(): List<NomenclatureItem> {
        return nomenclatureDao.getAll()
    }

    override suspend fun getInventarisationItems(): List<InvItem> = inventoryDao.selectAllSuspend()

    override suspend fun getNomenclatureBySearch(search: String): List<NomenclatureItem> =
        nomenclatureDao.getNomenclatureBySearch(search)

    override suspend fun updateInventoryQuantity(uid: Int, newQuantity: String) =
        inventoryDao.updateInventoryQuantity(uid, newQuantity)

    override suspend fun selectAllSuspend(): List<InvItem> = inventoryDao.selectAllSuspend()

    override suspend fun deleteAllNomenclature() = nomenclatureDao.deleteAll()

    override suspend fun insertNomenclature(nomenclatureList: List<NomenclatureItem>) =
        nomenclatureDao.insertNomenclature(nomenclatureList)

    override suspend fun deleteAllInventory() = inventoryDao.deleteAll()

    override suspend fun loadInventoryGrouped(): List<InvItem> =
        inventoryDao.loadInventoryGrouped()

    override fun selectInventoryItemByCode(code: String): String? =
        inventoryDao.selectInventoryItemByCode(code)

    override fun selectNomenclatureItemByCode(code: String): NomenclatureItem? =
        nomenclatureDao.selectNomenclatureItemByCode(code)

    override fun selectInventoryItemByPLUCode(plu: String): String? =
        inventoryDao.selectInventoryItemByPLUCode(plu)

    override fun selectNomenclatureItemByPLUCode(plu: String): NomenclatureItem? =
        nomenclatureDao.selectNomenclatureItemByPLUCode(plu)

    override fun selectInventoryItemByBarcode(barcode: String): String? =
        inventoryDao.selectInventoryItemByBarcode(barcode)

    override fun selectNomenclatureItemByBarcode(barcode: String): NomenclatureItem? =
        nomenclatureDao.selectNomenclatureItemByBarcode(barcode)

    override suspend fun insertInventory(invItem: InvItem) = inventoryDao.insert(invItem)

    override suspend fun getItemForDetail(code: String, barcode: String): NomenclatureItem? =
        nomenclatureDao.getItemForDetail(code, barcode)
}
