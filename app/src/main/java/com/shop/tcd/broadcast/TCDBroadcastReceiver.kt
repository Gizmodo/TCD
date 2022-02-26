package com.shop.tcd.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import timber.log.Timber

class TCDBroadcastReceiver : BroadcastReceiver() {
    var listener: TCDBroadcastListener? = null
    override fun onReceive(context: Context?, intent: Intent?) {

        when (intent?.action) {
            "android.intent.action.SCANRESULT" -> {
//              iData barcode
                val extras = intent.extras
                enumerateBundles(extras)
                val res = extras?.keySet()?.contains("value")
                if (res == true) {
                    val barcode = extras["value"].toString()
                    listener?.onSuccess(barcode)
                } else {
                    listener?.onError("iData barcode error")
                }
            }
            "android.intent.ACTION_DECODE_DATA" -> {
//              Urovo barcode
                val extras = intent.extras
                enumerateBundles(extras)
                val res = extras?.keySet()?.contains("barcode_string")
                if (res == true) {
                    val barcode = extras["barcode_string"].toString()
                    listener?.onSuccess(barcode)
                } else {
                    listener?.onError("Urovo barcode error")
                }
            }
            "android.intent.action_keyboard" -> {
//              Urovo keyboard
                val extras = intent.extras
                enumerateBundles(extras)
                val res = extras?.keySet()?.contains("kbrd_enter")
                if (res == true) {
                    val barcode = extras["kbrd_enter"].toString()
                    listener?.onSuccess(barcode)
                } else {
                    listener?.onError("Urovo keyboard error")
                }
            }
        }

    }

    private fun enumerateBundles(extras: Bundle?) {
        extras?.let {
            for (key: String in it.keySet()) {
                Timber.d("Key=" + key + " extras=" + it[key])
            }
        }
    }
}