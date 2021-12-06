package com.shop.tcd.repository.main

import com.shop.tcd.model.post.Payload

class RepositoryMain constructor(private val retrofitServiceMain: RetrofitServiceMain) {
    /**
     * Загрузить список групп товаров
     */
    fun getAllGroups() = retrofitServiceMain.getAllGroups()

    /**
     * Загрузить товары по выбранным группам
     */
    fun getByGroup(filterString: String) = retrofitServiceMain.getByGroup(filterString)

    /**
     * Загрузить весь список товаров
     */
    fun getAllItems() = retrofitServiceMain.getAllItems()

    /**
     * Загрузить товары по остаткам
     */
    fun getRemainders() = retrofitServiceMain.getRemainders()

    /**
     * Загрузить товары за период
     */
    fun getPeriod(filterString: String) = retrofitServiceMain.getPeriod(filterString)

    /**
     * Отправить ответ по инвентаризации
     */
    fun postInventory(payload: Payload) = retrofitServiceMain.postInventory("", payload)

    /**
     * Тестовый метод с приемом полного Endpoint
     */
//    fun getAllGroupsUrl(url: String) = RetrofitMain.getAllGroupsURL(url)
}