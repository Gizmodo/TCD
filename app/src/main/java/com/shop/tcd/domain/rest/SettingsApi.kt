package com.shop.tcd.domain.rest

import com.shop.tcd.data.printer.PrintersList
import com.shop.tcd.data.shop.ShopsList
import com.shop.tcd.data.user.UsersList
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
