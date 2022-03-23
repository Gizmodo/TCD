package com.shop.tcd.v2.core.extension

import android.app.Activity
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
import android.view.inputmethod.InputMethodManager

fun Activity.hideSoftKeyboardExt() {
    window.setSoftInputMode(SOFT_INPUT_STATE_HIDDEN)
    currentFocus?.apply {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }
}