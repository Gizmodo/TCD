package com.shop.tcd.repository.main

import com.shop.tcd.model.post.Payload

class RepositoryMain constructor(private val retrofitServiceMain: RetrofitServiceMain) {
    /**
     * Отправить ответ по инвентаризации
     */
    fun postInventory(payload: Payload) = retrofitServiceMain.postInventory("", payload)
}