package com.shop.tcd.broadcast

interface TCDBroadcastListener {
    fun onSuccess(message: String)
    fun onError(message: String)
}