package com.shop.tcd.core.utils

object Util {
    fun String.isValidServerAddress(): Boolean {
        val regex =
            "(http://|https://)(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(:[0-9]{1,5})?(/.*)"
                .toRegex()
        return regex.matches(this)
    }
}