package com.shop.tcd.data.repository

import com.shop.tcd.core.extension.NetworkResult
import com.shop.tcd.core.extension.handleApi
import com.shop.tcd.data.group.GroupsList
import com.shop.tcd.data.nomenclature.NomenclatureList
import com.shop.tcd.data.pricetag.PriceTag
import com.shop.tcd.data.pricetag.response.PriceTagResponse
import com.shop.tcd.domain.rest.ShopApi
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
}