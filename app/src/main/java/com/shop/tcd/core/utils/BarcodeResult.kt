package com.shop.tcd.core.utils

sealed class ResponseState<out T> {
    //    object Loading : ResponseState<Nothing>()
    data class Error(val throwable: Throwable) : ResponseState<Nothing>()
    data class Success<T>(val item: T) : ResponseState<T>()
}

class InvalidBarcodeException(message: String) : Exception(message)
