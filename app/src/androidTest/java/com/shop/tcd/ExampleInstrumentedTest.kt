package com.shop.tcd

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.shop.tcd.ato.OneTimeWorker
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.ExecutionException

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    private var context: Context = ApplicationProvider.getApplicationContext()
    private var workManager: WorkManager? = null

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        workManager = WorkManager.getInstance(context)
        val config: Configuration = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context, config
        )
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun createWork() {
        val worker: OneTimeWorker =
            TestListenableWorkerBuilder.from(context, OneTimeWorker::class.java).build()

        val result: ListenableWorker.Result = worker.startWork().get()

//        assertEquals(result, ListenableWorker.Result.success())
        /* assertEquals(
             (result as ListenableWorker.Result.Success).outputData.getString("filename"),
             "TCD-1.3.0.BN-1322.apk"
         )*/
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.shop.tcd", appContext.packageName)
    }
}
