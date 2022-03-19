package com.shop.tcd.v2

import android.content.Context
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.shop.tcd.v2.core.utils.Common
import com.shop.tcd.v2.core.utils.TimeSliceExecutor
import timber.log.Timber
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.UnknownHostException
import java.util.concurrent.atomic.AtomicBoolean

class ExampleWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        runHeavyTask()
        return Result.success()
    }

    private fun runHeavyTask() {
        Thread(runnable).start()
    }

    private val working = AtomicBoolean(true)
    private lateinit var socket: Socket

    private var dataOutputStream: DataOutputStream? = null
    private var message = ""
    private var ip = ""

    @Throws(IOException::class)
    private fun buildSocket(): Socket {
        val socket = Socket()
        socket.soTimeout = Common.TCP_SERVICE_TCP_TIMEOUT_INT
        socket.connect(
            InetSocketAddress(
                resolveHost(ip, Common.TCP_SERVICE_DNS_TIMEOUT),
                Common.TCP_SERVICE_PORT
            ),
            Common.TCP_SERVICE_TCP_TIMEOUT_INT
        )
        return socket
    }

    @Throws(IOException::class, UnknownHostException::class)
    private fun resolveHost(host: String, dnsTimeout: Long): InetAddress? {
        return try {
            TimeSliceExecutor.execute<InetAddress>({
                InetAddress.getByName(host)
            }, dnsTimeout)
        } catch (ex: TimeSliceExecutor.TimeSliceExecutorException) {
            throw UnknownHostException(host)
        }
    }

    private val runnable: Runnable = Runnable {
        try {
            Timber.d("Runnable")
            socket = buildSocket()
            dataOutputStream = DataOutputStream(socket.getOutputStream())
            while (working.get()) {
                try {
                    dataOutputStream!!.writeUTF(message)
                    Thread.sleep(Common.TCP_SERVICE_THREAD_TIMEOUT)
                    dataOutputStream!!.close()
                    working.set(false)
                    stopWorkManager(appContext)
                } catch (e: IOException) {
                    e.printStackTrace()
                    try {
                        dataOutputStream!!.close()
                        stopWorkManager(appContext)
                    } catch (ex: IOException) {
                        ex.printStackTrace()
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    try {
                        dataOutputStream!!.close()
                        stopWorkManager(appContext)
                    } catch (ex: IOException) {
                        ex.printStackTrace()
                    }
                }
            }
            stopWorkManager(appContext)
        } catch (e: IOException) {
            Timber.d("Runnable")
            stopWorkManager(appContext)
        }
    }

    private fun stopWorkManager(appContext: Context) {
        WorkManager.getInstance(appContext).cancelAllWork()
    }
}