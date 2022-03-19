@file:Suppress("unused")

package com.shop.tcd

import android.app.Application
import android.content.Context
import com.bugsnag.android.Bugsnag
import com.bugsnag.android.Configuration
import com.bugsnag.android.okhttp.BugsnagOkHttpPlugin
import com.shop.tcd.v2.core.utils.LineNumberDebugTree
import leakcanary.LeakCanary
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
        val bugsnagOkHttpPlugin = BugsnagOkHttpPlugin()
        val config = Configuration.load(this)
        config.addPlugin(bugsnagOkHttpPlugin)
        Bugsnag.start(this, config)
        LeakCanary.config = LeakCanary.config.copy(
            onHeapAnalyzedListener = BugsnagLeakUploader(applicationContext = this)
        )
        if (BuildConfig.DEBUG) {
            Timber.plant(LineNumberDebugTree())
        }
        /* appGraph=DaggerAppGraph
             .Builder()*/
    }
}