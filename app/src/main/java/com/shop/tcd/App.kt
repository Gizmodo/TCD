@file:Suppress("unused")

package com.shop.tcd

import android.app.Application
import android.content.Context
import com.bugsnag.android.Bugsnag
import com.bugsnag.android.Configuration
import com.bugsnag.android.okhttp.BugsnagOkHttpPlugin
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class App : Application() {
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

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
        val bugsnagOkHttpPlugin = BugsnagOkHttpPlugin()
        val config = Configuration.load(this)
        config.addPlugin(bugsnagOkHttpPlugin)
        Bugsnag.start(this, config)
        /*  LeakCanary.config = LeakCanary.config.copy(
              onHeapAnalyzedListener = BugsnagLeakUploader(applicationContext = this)
          )*/
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        if (BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
            Timber.plant(LineNumberDebugTree())
        } else {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
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
