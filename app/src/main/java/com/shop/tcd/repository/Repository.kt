package com.shop.tcd.repository

import com.shop.tcd.model.post.Payload

class Repository constructor(private val retrofitService: RetrofitService) {
    /**
     * Загрузить список групп товаров
     */
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

    /**
     * Загрузить товары по выбранным группам
     */
    fun getByGroup(filterString: String) = retrofitService.getByGroup(filterString)

    /**
     * Отправить ответ по инвентаризации
     */
    fun postInventory(payload: Payload) = retrofitService.postInventory("", payload)

    /**
     * Загрузить настройки
     */
    fun getSettings() = retrofitService.getSettings()

    fun getxml() =retrofitService.getxml()
}