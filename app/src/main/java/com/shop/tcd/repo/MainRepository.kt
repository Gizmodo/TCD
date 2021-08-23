package com.shop.tcd.repo

import com.shop.tcd.retro.RetrofitService

class MainRepository constructor(private val retrofitService: RetrofitService) {
    fun getAllGoods() = retrofitService.getAllGoods()
    fun getAllGroups() = retrofitService.getAllGroups()
}