@file:Suppress("unused")

package com.shop.tcd

import android.app.Application
import com.shop.tcd.utils.LineNumberDebugTree
import timber.log.Timber


class App : Application() {
//    lateinit var appGraph: AppGraph
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(LineNumberDebugTree())
        }
       /* appGraph=DaggerAppGraph
            .Builder()*/
    }
}