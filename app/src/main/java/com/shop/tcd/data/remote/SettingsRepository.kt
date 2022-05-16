package com.shop.tcd.data.remote

import com.shop.tcd.core.extension.NetworkResult
import com.shop.tcd.core.extension.handleApi
import com.shop.tcd.data.dto.printer.PrintersList
import com.shop.tcd.data.dto.shop.ShopsList
import com.shop.tcd.data.dto.user.UsersList
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val apiService: SettingsApi,
) {
    suspend operator fun invoke(prefix: String): NetworkResult<PrintersList> =
        handleApi { apiService.getPrinters(prefix) }

    suspend fun printers(prefix: String): NetworkResult<PrintersList> =
        handleApi { apiService.getPrinters(prefix) }

    suspend fun shops(): NetworkResult<ShopsList> =
        handleApi { apiService.getShopsSuspend() }

    suspend fun users(): NetworkResult<UsersList> =
        handleApi { apiService.getUsersSuspend() }
}