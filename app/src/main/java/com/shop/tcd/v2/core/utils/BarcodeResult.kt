package com.shop.tcd.v2.core.utils

sealed class BarcodeResult_T<T> {
    data class Success<T>(var data: T) : BarcodeResult_T<T>()
    data class Failure<T>(val error: String) : BarcodeResult_T<T>()
}

sealed class BarcodeResult1(val result: String) {
    data class Success(val message: String) : BarcodeResult1(message)
    data class Failure(val error: String) : BarcodeResult1(error)
}
sealed class ResponseState<out T> {
//    object Loading : ResponseState<Nothing>()
    data class Error(val throwable: Throwable) : ResponseState<Nothing>()
    data class Success<T>(val item: T) : ResponseState<T>()
}
class InvalidBarcodeException(message: String) : Exception(message)
