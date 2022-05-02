package com.shop.tcd.core.utils

import com.shop.tcd.data.printer.Printer
import com.shop.tcd.data.shop.ShopModel
import com.shop.tcd.data.user.UserModel
import com.shop.tcd.domain.SearchType

class Constants {
    object Inventory {
        const val DEBOUNCE_TIME = 100L
        const val BARCODE_LENGTH = 13
        const val BARCODE_LENGTH_WO_PREFIX = 11
        const val BARCODE_LENGTH_WO_CRC = 12
        const val BARCODE_LENGTH_WEIGHT_SUFFIX = 5
        const val BARCODE_LENGTH_PREFIX = 2
        const val CODE_LENGTH = 5
    }

    object Network {
        const val BASE_URL = "http://192.168.88.87:3000/"
        var TEST_SERVER_1 = "http://10.254.1.230:3000/"
        var TEST_SERVER_2 = "http://192.168.88.87:3000/"
        var PROD_SERVER_1 = "http://192.168.0.154/"

        /**
         * Изменяемый адрес, который указывает на выбранный магазин (сервер 1С)
         */
        var BASE_SHOP_URL = ""
        const val OK_HTTP_TIMEOUT = 15L
    }

    object TCP {
        const val TCP_SERVICE_TCP_TIMEOUT_INT = 2000
        const val TCP_SERVICE_PORT = 9100
        const val TCP_SERVICE_DNS_TIMEOUT = 2000L
        const val TCP_SERVICE_THREAD_TIMEOUT = 2000L
    }

    object Animation {
        const val ANIMATION_TIMEOUT = 1000L
        const val ANIMATION_FROM_DEGREE = 0f
        const val ANIMATION_TO_DEGREE = 360f
        const val ANIMATION_PIVOT = 0.5f
    }

    object SelectedObjects {
        /**
         * Хранение префикса и
         * позиция поиска веса и кода
         */
        lateinit var shopTemplate: ShopTemplate

        /**
         * Хранение выбранного магазина
         */
        lateinit var ShopModel: ShopModel
        var ShopModelPosition: Int = -1

        /**
         * Хранение выбранного пользователя и его позиции
         */
        lateinit var UserModel: UserModel
        var UserModelPosition: Int = -1

        /**
         * Хранение выбранного принтера и его позиции
         */
        lateinit var PrinterModel: Printer
        var PrinterModelPosition: Int = -1
    }

    data class ShopTemplate(
        /*
            П - PLU
            Т - Код
            М - Вес
        */
        val prefix: String,
        val weightPosition: Pair<Int, Int>,
        val infoPosition: Pair<Int, Int>,
        val searchType: SearchType = SearchType.Empty,
    )
}