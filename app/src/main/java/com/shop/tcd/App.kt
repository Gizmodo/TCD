package com.shop.tcd

import android.app.Application
import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.bugsnag.android.Bugsnag
import com.bugsnag.android.Configuration
import com.bugsnag.android.okhttp.BugsnagOkHttpPlugin
import com.huawei.agconnect.common.network.AccessNetworkManager
import com.huawei.agconnect.crash.AGConnectCrash
import com.shop.tcd.core.update.UpdateWorker
import com.shop.tcd.core.utils.BugsnagLeakUploader
import com.shop.tcd.core.utils.Constants.Notifications.WORKER_TAG
import com.shop.tcd.core.utils.Constants.Notifications.WORKER_TIMEOUT
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

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(LineNumberDebugTree())
        }
        Timber.d("------------------------Application create------------------------")
        AGConnectCrash.getInstance().enableCrashCollection(true)
        AccessNetworkManager.getInstance().setAccessNetwork(true)
        val bugsnagOkHttpPlugin = BugsnagOkHttpPlugin()
        val config = Configuration.load(this)
        config.addPlugin(bugsnagOkHttpPlugin)
        Bugsnag.start(this, config)
        LeakCanary.config = LeakCanary.config.copy(
            onHeapAnalyzedListener = BugsnagLeakUploader(applicationContext = this)
        )
        runUpdateWorker()
    }

    private fun runUpdateWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        // TODO: Удалить очистку всех задач после теста
        WorkManager.getInstance(this).cancelAllWork()

        val worker = PeriodicWorkRequestBuilder<UpdateWorker>(WORKER_TIMEOUT)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                WORKER_TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                worker
            )
    }

    inner class LineNumberDebugTree : Timber.DebugTree() {

        override fun createStackElementTag(element: StackTraceElement): String {
            return "X0(${element.fileName}:${element.lineNumber})"
        }

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            super.log(priority, "$tag", message, t)
        }
    }
}
