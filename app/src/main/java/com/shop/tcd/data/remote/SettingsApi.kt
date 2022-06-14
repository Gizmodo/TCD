package com.shop.tcd.data.remote

import com.shop.tcd.data.dto.ato.UpdateRequest
import com.shop.tcd.data.dto.ato.UpdateResponse
import com.shop.tcd.data.dto.printer.PrintersList
import com.shop.tcd.data.dto.shop.ShopsList
import com.shop.tcd.data.dto.user.UsersList
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface SettingsApi {
    @GET("Tech/hs/tsd/users/get")
    suspend fun getUsersSuspend(): Response<UsersList>

    @GET("Tech/hs/tsd/shops/get")
    suspend fun getShopsSuspend(): Response<ShopsList>

    @GET("Tech/hs/tsd/printers/get")
    suspend fun getPrinters(@Query("prefix") prefix: String): Response<PrintersList>

    /**
     * Отправить текущую версию приложения и узнать есть ли для неё обновление.
     *
     * @param updateRequest тело запроса
     */
    @POST("checkupdate")
    @Headers("Content-Type:application/json")
    suspend fun checkUpdatePost(@Body updateRequest: UpdateRequest): Response<UpdateResponse>

    /**
     * Отправить ping серверу автообновлений.
     *
     */
    @GET("ping")
    suspend fun sendPing(): Response<String>
}
