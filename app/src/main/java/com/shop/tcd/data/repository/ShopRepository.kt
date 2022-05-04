package com.shop.tcd.data.repository

import com.shop.tcd.core.extension.NetworkResult
import com.shop.tcd.core.extension.handleApi
import com.shop.tcd.data.pricetag.PriceTag
import com.shop.tcd.data.pricetag.response.PriceTagResponse
import com.shop.tcd.domain.rest.ShopApi
import javax.inject.Inject

class ShopRepository @Inject constructor(
    private val apiService: ShopApi,
) {
    suspend fun getPrintInfoByBarcodes(barcodesList: PriceTag): NetworkResult<PriceTagResponse> =
        handleApi { apiService.getPriceTag(barcodesList) }
}