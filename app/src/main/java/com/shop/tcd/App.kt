@file:Suppress("unused")

package com.shop.tcd

import android.app.Application
import android.content.Context
import com.shop.tcd.utils.LineNumberDebugTree
import timber.log.Timber


class App : Application() {
    init {
        instance = this
    }

    companion object {
        private var instance: App? = null
        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }

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