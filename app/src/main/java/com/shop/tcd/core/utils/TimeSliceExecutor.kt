package com.shop.tcd.core.utils

import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

object TimeSliceExecutor {
    fun execute(runnable: Runnable?, timeoutInMillis: Long) {
        val executor = Executors.newSingleThreadExecutor()
        try {
            val future = executor!!.submit(runnable)
            getFuture(future, timeoutInMillis)
        } finally {
            executor?.shutdown()
        }
    }

    fun <T> execute(callable: Callable<T>?, timeoutInMillis: Long): T {
        val executor = Executors.newSingleThreadExecutor()
        return try {
            val future = executor!!.submit(callable)
            getFuture(future, timeoutInMillis)
        } finally {
            executor?.shutdown()
        }
    }

    fun <T> getFuture(future: Future<T>, timeoutInMillis: Long): T {
        return try {
            future[timeoutInMillis, TimeUnit.MILLISECONDS]
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
            throw TimeSliceExecutorException("Interrupton exception", ex)
        } catch (ex: ExecutionException) {
            throw TimeSliceExecutorException("Execution exception", ex)
        } catch (ex: TimeoutException) {
            throw TimeSliceExecutorException(
                String.format("%dms timeout reached", timeoutInMillis),
                ex
            )
        }
    }

    class TimeSliceExecutorException(message: String?, cause: Throwable?) :
        RuntimeException(message, cause)
}
