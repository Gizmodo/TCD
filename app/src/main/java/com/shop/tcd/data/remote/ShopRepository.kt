package com.shop.tcd.data.remote

import com.shop.tcd.core.extension.NetworkResult
import com.shop.tcd.core.extension.handleApi
import com.shop.tcd.data.dto.group.GroupsList
import com.shop.tcd.data.dto.inventory.InventoryResult
import com.shop.tcd.data.dto.nomenclature.NomenclatureList
import com.shop.tcd.data.dto.pricetag.PriceTag
import com.shop.tcd.data.dto.pricetag.response.PriceTagResponse
import javax.inject.Inject

class ShopRepository @Inject constructor(
    private val apiService: ShopApi,
) {
    suspend fun getPrintInfoByBarcodes(barcodesList: PriceTag): NetworkResult<PriceTagResponse> =
        handleApi { apiService.getPriceTag(barcodesList) }

    suspend fun getNomenclatureFull(): NetworkResult<NomenclatureList> =
        handleApi { apiService.getNomenclatureFull() }

    suspend fun getNomenclatureRemainders(): NetworkResult<NomenclatureList> =
        handleApi { apiService.getNomenclatureRemainders() }

    suspend fun getNomenclatureByPeriod(period: String): NetworkResult<NomenclatureList> =
        handleApi { apiService.getNomenclatureByPeriod(period) }

    suspend fun getGroupsList(): NetworkResult<GroupsList> =
        handleApi { apiService.getGroupsList() }

    suspend fun getNomenclatureByGroup(filter: String): NetworkResult<NomenclatureList> =
        handleApi { apiService.getNomenclatureByGroup(filter) }

    suspend fun postInventory(data: InventoryResult): NetworkResult<String> =
        handleApi { apiService.postInventory("", data) }
}