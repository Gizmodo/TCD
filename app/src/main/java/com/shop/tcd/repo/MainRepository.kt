package com.shop.tcd.repo

import com.shop.tcd.retro.RetrofitService

class MainRepository constructor(private val retrofitService: RetrofitService) {
    //Получить список групп товаров
    fun getAllGroups() = retrofitService.getAllGroups()

    /**
     * Загрузить весь список товаров
     */
    fun getAllItems() = retrofitService.getAllItems()

    /**
     * Загрузить товары по остаткам
     */
    fun getRemainders() = retrofitService.getRemainders()

    /**
     * Загрузить товары за период
     */
    fun getPeriod(filterString: String) = retrofitService.getPeriod(filterString)

}