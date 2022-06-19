package com.shop.tcd.core.utils

import android.app.Application
import com.bugsnag.android.Bugsnag
import com.bugsnag.android.Client
import com.bugsnag.android.Configuration
import com.bugsnag.android.ErrorTypes
import com.bugsnag.android.Event
import com.bugsnag.android.ThreadSendPolicy
import leakcanary.DefaultOnHeapAnalyzedListener
import leakcanary.OnHeapAnalyzedListener
import shark.HeapAnalysis
import shark.HeapAnalysisFailure
import shark.HeapAnalysisSuccess
import shark.Leak
import shark.LeakTrace
import shark.LibraryLeak

class BugsnagLeakUploader(applicationContext: Application) :
    OnHeapAnalyzedListener {

    private val defaultLeakListener = DefaultOnHeapAnalyzedListener.create()
    private val bugsnagClient: Client

    init {
        bugsnagClient = Bugsnag.start(
            applicationContext,
            Configuration(BUGSNAG_API_KEY).apply {
                enabledErrorTypes = DISABLE_ALL_ERROR_TYPES
                sendThreads = ThreadSendPolicy.NEVER
            }
        )
    }

    override fun onHeapAnalyzed(heapAnalysis: HeapAnalysis) {
        // Delegate to default behavior (notification and saving result)
        defaultLeakListener.onHeapAnalyzed(heapAnalysis)

        when (heapAnalysis) {
            is HeapAnalysisSuccess -> {
                val allLeakTraces = heapAnalysis
                    .allLeaks
                    .toList()
                    .flatMap { leak ->
                        leak.leakTraces.map { leakTrace -> leak to leakTrace }
                    }

                allLeakTraces.forEach { (leak, leakTrace) ->
                    val exception = FakeReportingException(leak.shortDescription)
                    bugsnagClient.notify(exception) { event ->
                        event.addHeapAnalysis(heapAnalysis)
                        event.addLeak(leak)
                        event.addLeakTrace(leakTrace)
                        event.groupingHash = leak.signature
                        true
                    }
                }
            }
            is HeapAnalysisFailure -> {
                bugsnagClient.notify(heapAnalysis.exception)
            }
        }
    }

    private fun Event.addHeapAnalysis(heapAnalysis: HeapAnalysisSuccess) {
        addMetadata("Leak", "heapDumpPath", heapAnalysis.heapDumpFile.absolutePath)
        heapAnalysis.metadata.forEach { (key, value) ->
            addMetadata("Leak", key, value)
        }
        addMetadata("Leak", "analysisDurationMs", heapAnalysis.analysisDurationMillis)
    }

    private fun Event.addLeak(leak: Leak) {
        addMetadata("Leak", "libraryLeak", leak is LibraryLeak)
        if (leak is LibraryLeak) {
            addMetadata("Leak", "libraryLeakPattern", leak.pattern.toString())
            addMetadata("Leak", "libraryLeakDescription", leak.description)
        }
    }

    private fun Event.addLeakTrace(leakTrace: LeakTrace) {
        addMetadata("Leak", "retainedHeapByteSize", leakTrace.retainedHeapByteSize)
        addMetadata("Leak", "signature", leakTrace.signature)
        addMetadata("Leak", "leakTrace", leakTrace.toString())
    }

    class FakeReportingException(message: String) : RuntimeException(message)

    companion object {
        private const val BUGSNAG_API_KEY = "d13ea502d3889a6d99f955103ef7e09c"
        private val DISABLE_ALL_ERROR_TYPES = ErrorTypes(
            anrs = false,
            ndkCrashes = false,
            unhandledExceptions = false,
            unhandledRejections = false
        )
    }
}
