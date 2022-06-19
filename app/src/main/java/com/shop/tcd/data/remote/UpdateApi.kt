package com.shop.tcd.data.remote

import com.shop.tcd.data.dto.ato.UpdateRequest
import com.shop.tcd.data.dto.ato.UpdateResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface UpdateApi {
    /**
     * Отправить текущую версию приложения и узнать есть ли для неё обновление.
     *
     * @param updateRequest тело запроса
     */
    @POST("checkupdate")
    @Headers("Content-Type:application/json")
    suspend fun checkUpdatePost(@Body updateRequest: UpdateRequest): Response<UpdateResponse>
}
