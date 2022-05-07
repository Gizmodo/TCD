package com.shop.tcd.data.repository

import com.shop.tcd.core.extension.NetworkResult
import com.shop.tcd.core.extension.handleApi
import com.shop.tcd.data.dto.group.GroupsList
import com.shop.tcd.data.dto.inventory.InvItem
import com.shop.tcd.data.dto.inventory.InventoryResult
import com.shop.tcd.data.dto.nomenclature.NomenclatureItem
import com.shop.tcd.data.dto.nomenclature.NomenclatureList
import com.shop.tcd.data.dto.pricetag.PriceTag
import com.shop.tcd.data.dto.pricetag.response.PriceTagResponse
import com.shop.tcd.data.dto.printer.PrintersList
import com.shop.tcd.data.dto.shop.ShopsList
import com.shop.tcd.data.dto.user.UsersList
import com.shop.tcd.data.local.InventoryDao
import com.shop.tcd.data.local.NomenclatureDao
import com.shop.tcd.data.remote.SettingsApi
import com.shop.tcd.data.remote.ShopApi
import javax.inject.Inject

class Repository @Inject constructor(
    private val inventoryDao: InventoryDao,
    private val nomenclatureDao: NomenclatureDao,
    private val shopApi: ShopApi,
    private val settingsApi: SettingsApi,
) : IRepository {
    override suspend fun invoke(): NetworkResult<PrintersList> =
        handleApi { settingsApi.getPrinters() }

    override suspend fun printers(): NetworkResult<PrintersList> =
        handleApi { settingsApi.getPrinters() }

    override suspend fun shops(): NetworkResult<ShopsList> =
        handleApi { settingsApi.getShopsSuspend() }

    override suspend fun users(): NetworkResult<UsersList> =
        handleApi { settingsApi.getUsersSuspend() }

    //*************************************************************************************************
    override suspend fun getPrintInfoByBarcodes(barcodesList: PriceTag): NetworkResult<PriceTagResponse> =
        handleApi { shopApi.getPriceTag(barcodesList) }

    override suspend fun getNomenclatureFull(): NetworkResult<NomenclatureList> =
        handleApi { shopApi.getNomenclatureFull() }

    override suspend fun getNomenclatureRemainders(): NetworkResult<NomenclatureList> =
        handleApi { shopApi.getNomenclatureRemainders() }

    override suspend fun getNomenclatureByPeriod(period: String): NetworkResult<NomenclatureList> =
        handleApi { shopApi.getNomenclatureByPeriod(period) }

    override suspend fun getGroupsList(): NetworkResult<GroupsList> =
        handleApi { shopApi.getGroupsList() }

    override suspend fun getNomenclatureByGroup(filter: String): NetworkResult<NomenclatureList> =
        handleApi { shopApi.getNomenclatureByGroup(filter) }

    override suspend fun postInventory(data: InventoryResult): NetworkResult<String> =
        handleApi { shopApi.postInventory("", data) }

    override suspend fun getAll(): List<NomenclatureItem> {
        return nomenclatureDao.getAll()
    }
//*************************************************************************************************


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

    override fun selectInventoryItemByCode(code: String): InvItem? =
        inventoryDao.selectInventoryItemByCode(code)

    override fun selectNomenclatureItemByCode(code: String): NomenclatureItem? =
        nomenclatureDao.selectNomenclatureItemByCode(code)

    override fun selectInventoryItemByPLUCode(plu: String): InvItem? =
        inventoryDao.selectInventoryItemByPLUCode(plu)

    override fun selectNomenclatureItemByPLUCode(plu: String): NomenclatureItem? =
        nomenclatureDao.selectNomenclatureItemByPLUCode(plu)

    override fun selectInventoryItemByBarcode(barcode: String): InvItem? =
        inventoryDao.selectInventoryItemByBarcode(barcode)

    override fun selectNomenclatureItemByBarcode(barcode: String): NomenclatureItem? =
        nomenclatureDao.selectNomenclatureItemByBarcode(barcode)

    override suspend fun insertInventory(invItem: InvItem) = inventoryDao.insert(invItem)

    override suspend fun getItemForDetail(code: String, barcode: String): NomenclatureItem? =
        nomenclatureDao.getItemForDetail(code, barcode)
}