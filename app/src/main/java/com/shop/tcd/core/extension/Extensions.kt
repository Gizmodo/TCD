package com.shop.tcd.core.extension

fun <T : Any> T?.notNull(f: (it: T) -> Unit) {
    if (this != null) f(this)
}