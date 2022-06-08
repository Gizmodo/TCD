package com.shop.tcd.core.utils

sealed class ShimmerState {
    object Empty : ShimmerState()
    object Loading : ShimmerState()
    data class State(val result: String) : ShimmerState()
    object Finishing : ShimmerState()
}
