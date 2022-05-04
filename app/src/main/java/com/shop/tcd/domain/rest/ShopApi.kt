package com.shop.tcd.domain.rest

import com.shop.tcd.data.group.GroupsList
import com.shop.tcd.data.inventory.InventoryResult
import com.shop.tcd.data.nomenclature.NomenclatureList
import com.shop.tcd.data.pricetag.PriceTag
import com.shop.tcd.data.pricetag.response.PriceTagResponse
import retrofit2.Response
import retrofit2.http.*

interface ShopApi {
    @GET("getitemlist")
    suspend fun getNomenclatureFull(): Response<NomenclatureList>

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

    @GET("getgrouplist")
    suspend fun getGroupsList(): Response<GroupsList>

    @GET("getgrouplist")
    suspend fun getNomenclatureByGroup(@Query("filter") filter: String): Response<NomenclatureList>

    @POST("receiveddocument")
    @Headers("Content-Type:application/json")
    suspend fun postInventory(
        @Query("filter") filter: String = "",
        @Body document: InventoryResult,
    ): Response<String>
}