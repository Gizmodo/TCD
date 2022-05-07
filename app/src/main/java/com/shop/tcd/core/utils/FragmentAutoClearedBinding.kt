package com.shop.tcd.core.utils

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class FragmentAutoClearedBinding<T : ViewBinding>(
    val binder: (View) -> T,
) : ReadOnlyProperty<Fragment, T>,
    DefaultLifecycleObserver {

    private var value: T? = null

    override fun onDestroy(owner: LifecycleOwner) {
        value = null
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        return value ?: binder(thisRef.requireView()).also {
            setValue(thisRef, it)
        }
    }

    private fun setValue(fragment: Fragment, value: T) {
        fragment.viewLifecycleOwner.lifecycle.removeObserver(this)
        this.value = value
        fragment.viewLifecycleOwner.lifecycle.addObserver(this)
    }
}