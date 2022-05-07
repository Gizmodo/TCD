package com.shop.tcd.data.repository

import com.shop.tcd.core.extension.NetworkResult
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

interface IRepository {
    suspend operator fun invoke(): NetworkResult<PrintersList>

    suspend fun printers(): NetworkResult<PrintersList>

    suspend fun shops(): NetworkResult<ShopsList>

    suspend fun users(): NetworkResult<UsersList>

    //*************************************************************************************************
    suspend fun getPrintInfoByBarcodes(barcodesList: PriceTag): NetworkResult<PriceTagResponse>

    suspend fun getNomenclatureFull(): NetworkResult<NomenclatureList>

    suspend fun getNomenclatureRemainders(): NetworkResult<NomenclatureList>

    suspend fun getNomenclatureByPeriod(period: String): NetworkResult<NomenclatureList>

    suspend fun getGroupsList(): NetworkResult<GroupsList>

    suspend fun getNomenclatureByGroup(filter: String): NetworkResult<NomenclatureList>

    suspend fun postInventory(data: InventoryResult): NetworkResult<String>
    //*************************************************************************************************

    suspend fun getAll(): List<NomenclatureItem>

    suspend fun getInventarisationItems(): List<InvItem>
    suspend fun getNomenclatureBySearch(search: String): List<NomenclatureItem>
    suspend fun updateInventoryQuantity(uid: Int, newQuantity: String)
    suspend fun selectAllSuspend(): List<InvItem>
    suspend fun deleteAllNomenclature()
    suspend fun insertNomenclature(nomenclatureList: List<NomenclatureItem>)
    suspend fun deleteAllInventory()
    suspend fun loadInventoryGrouped(): List<InvItem>
    fun selectInventoryItemByCode(code: String): InvItem?
    fun selectNomenclatureItemByCode(code: String): NomenclatureItem?
    fun selectInventoryItemByPLUCode(plu: String): InvItem?
    fun selectNomenclatureItemByPLUCode(plu: String): NomenclatureItem?
    fun selectInventoryItemByBarcode(barcode: String): InvItem?
    fun selectNomenclatureItemByBarcode(barcode: String): NomenclatureItem?
    suspend fun insertInventory(invItem: InvItem)
    suspend fun getItemForDetail(code: String, barcode: String): NomenclatureItem?
}