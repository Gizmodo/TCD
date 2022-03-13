package com.shop.tcd.repository.network.settings

import com.shop.tcd.model.newsettigs.PrintersList
import com.shop.tcd.model.newsettigs.UserListItem
import io.reactivex.rxjava3.core.Observable
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET

interface SettingsApi {
    @GET("SettingsForTSD.xml")
    fun getSettings(): Call<String>

    @GET("Tech/hs/tsd/users/get")
    fun getUsersObservable(): Observable<List<UserListItem>>
    @GET("Tech/hs/tsd/users/get")
    suspend fun getUsersSuspend(): Response<List<UserListItem>>

    @GET("Tech/hs/tsd/shops/get")
    fun getShops(): Observable<List<UserListItem>>

    @GET("Tech/hs/tsd/printers/get")
    fun getPrinters(): Observable<PrintersList>
}
