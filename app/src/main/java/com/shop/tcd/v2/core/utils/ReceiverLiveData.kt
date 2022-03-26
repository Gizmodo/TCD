package com.shop.tcd.v2.core.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.MutableLiveData
import io.reactivex.rxjava3.functions.BiFunction

class ReceiverLiveData<T : Any>(
    private val context: Context,
    private val filter: IntentFilter,
    private val mapFunc: BiFunction<Context, Intent, T>
) : MutableLiveData<T>() {

    override fun onInactive() {
        super.onInactive()
        context.unregisterReceiver(mBroadcastReceiver)
    }

    override fun onActive() {
        super.onActive()
        value = mapFunc.apply(context, Intent())
        context.registerReceiver(mBroadcastReceiver, filter)
    }

    private val mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            value = mapFunc.apply(context, intent)
        }
    }
}