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
     * Отправить ответ по инвентаризации
     */
    fun postInventory(payload: Payload) = retrofitServiceMain.postInventory("", payload)
}