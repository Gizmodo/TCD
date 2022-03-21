package com.shop.tcd.v2.domain.rest

import com.shop.tcd.model.Nomenclature
import com.shop.tcd.v2.data.pricetag.PriceTag
import com.shop.tcd.v2.data.pricetag.response.PriceTagResponse
import io.reactivex.rxjava3.core.Observable
import retrofit2.Response
import retrofit2.http.*

interface ShopApi {
    @GET("getitemlist")
    suspend fun getNomenclatureFull(): Response<Nomenclature>

    @GET("getiteminstock")
    suspend fun getNomenclatureRemainders(): Response<Nomenclature>

    @GET("getturnoverfortheperiod")
    suspend fun getNomenclatureByPeriod(@Query("filter") filter: String): Response<Nomenclature>

    @POST("pricetag/POST")
    @Headers("Content-Type:application/json")
    fun postPriceTag(@Body priceTag: PriceTag): Observable<Response<PriceTagResponse>>
}