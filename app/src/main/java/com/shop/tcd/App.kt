@file:Suppress("unused")

package com.shop.tcd

import android.app.Application
import android.content.Context
import com.bugsnag.android.Bugsnag
import com.bugsnag.android.Configuration
import com.bugsnag.android.okhttp.BugsnagOkHttpPlugin
import com.huawei.agconnect.common.network.AccessNetworkManager
import com.huawei.agconnect.crash.AGConnectCrash
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

    override fun onCreate() {
        super.onCreate()
        AGConnectCrash.getInstance().enableCrashCollection(true)
        AccessNetworkManager.getInstance().setAccessNetwork(true)
        val bugsnagOkHttpPlugin = BugsnagOkHttpPlugin()
        val config = Configuration.load(this)
        config.addPlugin(bugsnagOkHttpPlugin)
        Bugsnag.start(this, config)
        /*  LeakCanary.config = LeakCanary.config.copy(
              onHeapAnalyzedListener = BugsnagLeakUploader(applicationContext = this)
          )*/
        if (BuildConfig.DEBUG) {
            Timber.plant(LineNumberDebugTree())
        }
    }

    inner class LineNumberDebugTree : Timber.DebugTree() {

        override fun createStackElementTag(element: StackTraceElement) =
            "${element.fileName}:${element.lineNumber}"

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            super.log(priority, "$tag", message, t)
        }
    }
}
