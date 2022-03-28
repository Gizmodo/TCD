package com.shop.tcd.v2.domain.rest

import com.shop.tcd.v2.data.group.GroupsList
import com.shop.tcd.v2.data.inventory.Payload
import com.shop.tcd.v2.data.nomenclature.NomenclatureList
import com.shop.tcd.v2.data.pricetag.PriceTag
import com.shop.tcd.v2.data.pricetag.response.PriceTagResponse
import io.reactivex.rxjava3.core.Observable
import retrofit2.Response
import retrofit2.http.*

interface ShopApi {
    @GET("getitemlist")
    suspend fun getNomenclatureFull(): Response<NomenclatureList>

    @GET("getiteminstock")
    suspend fun getNomenclatureRemainders(): Response<NomenclatureList>

    @GET("getturnoverfortheperiod")
    suspend fun getNomenclatureByPeriod(@Query("filter") filter: String): Response<NomenclatureList>

    @POST("pricetag/POST")
    @Headers("Content-Type:application/json")
    fun postPriceTag(@Body priceTag: PriceTag): Observable<Response<PriceTagResponse>>

    @GET("getgrouplist")
    suspend fun getGroupsList(): Response<GroupsList>

    @GET("getgrouplist")
    suspend fun getNomenclatureByGroup(@Query("filter") filter: String): Response<NomenclatureList>

    @POST("receiveddocument")
    @Headers("Content-Type:application/json")
    fun postInventory(
        @Query("filter") filter: String = "",
        @Body document: Payload,
    ): Response<String>
}