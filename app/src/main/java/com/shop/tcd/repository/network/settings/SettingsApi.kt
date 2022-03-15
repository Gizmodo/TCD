package com.shop.tcd.repository.network.settings

import com.shop.tcd.v2.data.printer.PrintersList
import com.shop.tcd.v2.data.shop.ShopsList
import com.shop.tcd.v2.data.user.UsersList
import io.reactivex.rxjava3.core.Observable
import retrofit2.Response
import retrofit2.http.GET

interface SettingsApi {
    @GET("Tech/hs/tsd/users/get")
    suspend fun getUsersSuspend(): Response<UsersList>

    @GET("Tech/hs/tsd/shops/get")
    suspend fun getShopsSuspend(): Response<ShopsList>

    @GET("Tech/hs/tsd/printers/get")
    fun getPrinters(): Observable<PrintersList>
}
