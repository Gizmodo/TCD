package com.shop.tcd.data.remote

import com.shop.tcd.data.dto.printer.PrintersList
import com.shop.tcd.data.dto.shop.ShopsList
import com.shop.tcd.data.dto.user.UsersList
import retrofit2.Response
import retrofit2.http.GET

interface SettingsApi {
    @GET("Tech/hs/tsd/users/get")
    suspend fun getUsersSuspend(): Response<UsersList>

    @GET("Tech/hs/tsd/shops/get")
    suspend fun getShopsSuspend(): Response<ShopsList>

    @GET("Tech/hs/tsd/printers/get")
    suspend fun getPrinters(): Response<PrintersList>
}
