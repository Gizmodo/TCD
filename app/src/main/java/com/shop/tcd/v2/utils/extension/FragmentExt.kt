package com.shop.tcd.v2.utils

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.Navigation

fun Fragment.navigateExt(directions: NavDirections) {
    view?.let { Navigation.findNavController(it).navigate(directions) }
}

fun Fragment.navigateExt(@IdRes resId: Int) {
    view?.let { Navigation.findNavController(it).navigate(resId) }
}

fun Fragment.hideSoftKeyboardExt() {
    activity?.hideSoftKeyboardExt()
}