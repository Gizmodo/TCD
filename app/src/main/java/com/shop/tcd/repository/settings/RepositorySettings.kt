package com.shop.tcd.repository.settings

class RepositorySettings constructor(private val retrofitServiceSettings: RetrofitServiceSettings) {

    /**
     * Загрузить настройки
     */
    fun getSettings() = retrofitServiceSettings.getSettings()
}