package com.shop.tcd.core.extension

import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.viewbinding.ViewBinding
import com.shop.tcd.core.utils.FragmentAutoClearedBinding

fun Fragment.navigateExt(directions: NavDirections) {
    view?.let { Navigation.findNavController(it).navigate(directions) }
}

fun Fragment.navigateExt(@IdRes resId: Int) {
    view?.let { Navigation.findNavController(it).navigate(resId) }
}

fun Fragment.hideSoftKeyboardExt() {
    activity?.hideSoftKeyboardExt()
}

fun <T : ViewBinding> viewBindingWithBinder(binder: (View) -> T) =
    FragmentAutoClearedBinding(binder)