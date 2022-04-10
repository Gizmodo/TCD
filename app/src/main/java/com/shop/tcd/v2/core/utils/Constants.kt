package com.shop.tcd.v2.core.utils

class Constants {
    object Inventory {
        const val DEBOUNCE_TIME = 100L
        const val BARCODE_LENGTH = 13
        const val BARCODE_LENGTH_WO_PREFIX = 11
        const val BARCODE_LENGTH_WEIGHT_SUFFIX = 5
        const val CODE_LENGTH = 5
    }

    object Network {
        const val BASE_URL = "http://192.168.0.154/"
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
}