package com.shop.tcd.data.remote

import com.shop.tcd.core.extension.NetworkResult
import com.shop.tcd.core.extension.handleApi
import com.shop.tcd.data.dto.group.GroupsList
import com.shop.tcd.data.dto.inventory.InventoryResult
import com.shop.tcd.data.dto.nomenclature.NomenclatureList
import com.shop.tcd.data.dto.pricetag.PriceTag
import com.shop.tcd.data.dto.pricetag.response.PriceTagResponse
import com.shop.tcd.data.dto.remains.request.RemainsRequestBody
import com.shop.tcd.data.dto.remains.response.RemainsResponse
import javax.inject.Inject

class ShopRepository @Inject constructor(
    private val shopApi: ShopApi,
) {
    suspend fun getPrintInfoByBarcodes(barcodesList: PriceTag): NetworkResult<PriceTagResponse> =
        handleApi { shopApi.getPriceTag(barcodesList) }

    suspend fun getRemains(barcodesList: RemainsRequestBody): NetworkResult<RemainsResponse> =
        handleApi { shopApi.getRemains(barcodesList) }

    suspend fun getNomenclatureFull(prefix: String): NetworkResult<NomenclatureList> =
        handleApi { shopApi.getNomenclatureFull(prefix) }

    suspend fun getNomenclatureRemainders(): NetworkResult<NomenclatureList> =
        handleApi { shopApi.getNomenclatureRemainders() }

    suspend fun getNomenclatureByPeriod(period: String): NetworkResult<NomenclatureList> =
        handleApi { shopApi.getNomenclatureByPeriod(period) }

    suspend fun getGroupsList(prefix: String): NetworkResult<GroupsList> =
        handleApi { shopApi.getGroupsList(prefix) }

    suspend fun getNomenclatureByGroup(
        filter: String,
        prefix: String
    ): NetworkResult<NomenclatureList> =
        handleApi { shopApi.getNomenclatureByGroup(filter, prefix) }

    suspend fun postInventory(data: InventoryResult): NetworkResult<String> =
        handleApi { shopApi.postInventory("", data) }
}
