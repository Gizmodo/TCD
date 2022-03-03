package com.shop.tcd.repository.network.settings

import com.shop.tcd.model.newsettigs.UserListItem
import io.reactivex.rxjava3.core.Observable
import retrofit2.Call
import retrofit2.http.GET

interface SettingsApi {
    @GET("SettingsForTSD.xml")
    fun getSettings(): Call<String>

    @GET("Tech/hs/tsd/users/get")
    fun getUsers(): Observable<List<UserListItem>>

    @GET("Tech/hs/tsd/shops/get")
    fun getShops(): Observable<List<UserListItem>>
}
