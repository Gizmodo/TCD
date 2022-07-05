package com.shop.tcd.data.remote

import com.shop.tcd.data.dto.group.GroupsList
import com.shop.tcd.data.dto.inventory.InventoryResult
import com.shop.tcd.data.dto.nomenclature.NomenclatureList
import com.shop.tcd.data.dto.pricetag.PriceTag
import com.shop.tcd.data.dto.pricetag.response.PriceTagResponse
import com.shop.tcd.data.dto.refund.request.RefundRequestBody
import com.shop.tcd.data.dto.remains.request.RemainsRequestBody
import com.shop.tcd.data.dto.remains.response.RemainsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface ShopApi {
    @GET("getitemlist")
    suspend fun getNomenclatureFull(
        @Query("prefix") prefix: String
    ): Response<NomenclatureList>

    @GET("getiteminstock")
    suspend fun getNomenclatureRemainders(): Response<NomenclatureList>

    @GET("getturnoverfortheperiod")
    suspend fun getNomenclatureByPeriod(@Query("filter") filter: String): Response<NomenclatureList>

    /**
     * Запрос информации для печати ценников на основе списка ШК/кодов
     */
    @POST("pricetag/POST")
    @Headers("Content-Type:application/json")
    suspend fun getPriceTag(@Body priceTag: PriceTag): Response<PriceTagResponse>

    /**
     * Запрос информации остатков на основе ШК/кода
     */
    @POST("remains")
    @Headers("Content-Type:application/json")
    suspend fun getRemains(@Body body: RemainsRequestBody): Response<RemainsResponse>

    /**
     * Отправка ШК/DataMatrix/PDF417
     */
    @POST("doc")
    @Headers("Content-Type:application/json")
    suspend fun getRefund(@Body body: RefundRequestBody): Response<String>

    @GET("getgrouplist")
    suspend fun getGroupsList(
        @Query("prefix") prefix: String
    ): Response<GroupsList>

    @GET("getgrouplist")
    suspend fun getNomenclatureByGroup(
        @Query("filter") filter: String,
        @Query("prefix") prefix: String
    ): Response<NomenclatureList>

    @POST("receiveddocument")
    @Headers("Content-Type:application/json")
    suspend fun postInventory(
        @Query("filter") filter: String = "",
        @Body document: InventoryResult,
    ): Response<String>
}
