package com.shop.tcd.data.repository

import com.shop.tcd.core.extension.NetworkResult
import com.shop.tcd.core.extension.handleApi
import com.shop.tcd.data.printer.PrintersList
import com.shop.tcd.data.shop.ShopsList
import com.shop.tcd.data.user.UsersList
import com.shop.tcd.domain.rest.SettingsApi
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val apiService: SettingsApi,
) {
    suspend operator fun invoke(): NetworkResult<PrintersList> =
        handleApi { apiService.getPrinters() }

    suspend fun printers(): NetworkResult<PrintersList> =
        handleApi { apiService.getPrinters() }

    suspend fun shops(): NetworkResult<ShopsList> =
        handleApi { apiService.getShopsSuspend() }

    suspend fun users(): NetworkResult<UsersList> =
        handleApi { apiService.getUsersSuspend() }
}