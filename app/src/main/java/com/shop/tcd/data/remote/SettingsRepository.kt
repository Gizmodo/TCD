package com.shop.tcd.data.remote

import com.shop.tcd.core.extension.NetworkResult
import com.shop.tcd.core.extension.handleApi
import com.shop.tcd.data.dto.ato.UpdateRequest
import com.shop.tcd.data.dto.printer.PrintersList
import com.shop.tcd.data.dto.shop.ShopsList
import com.shop.tcd.data.dto.user.UsersList
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val settingsApi: SettingsApi,
) {
    suspend fun invoke(prefix: String): NetworkResult<PrintersList> =
        handleApi { settingsApi.getPrinters(prefix) }

    suspend fun printers(prefix: String): NetworkResult<PrintersList> =
        handleApi { settingsApi.getPrinters(prefix) }

    suspend fun shops(): NetworkResult<ShopsList> =
        handleApi { settingsApi.getShopsSuspend() }

    suspend fun users(): NetworkResult<UsersList> =
        handleApi { settingsApi.getUsersSuspend() }

    suspend fun pingUpdateServer(): NetworkResult<String> =
        handleApi { settingsApi.sendPing() }

    suspend fun checkUpdatePost(updateRequest: UpdateRequest) =
        handleApi { settingsApi.checkUpdatePost(updateRequest) }
}
