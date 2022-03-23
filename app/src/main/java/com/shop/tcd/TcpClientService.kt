package com.shop.tcd

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.shop.tcd.v2.core.utils.Common.TCP_SERVICE_DNS_TIMEOUT
import com.shop.tcd.v2.core.utils.Common.TCP_SERVICE_PORT
import com.shop.tcd.v2.core.utils.Common.TCP_SERVICE_TCP_TIMEOUT_INT
import com.shop.tcd.v2.core.utils.Common.TCP_SERVICE_THREAD_TIMEOUT
import com.shop.tcd.v2.core.utils.TimeSliceExecutor
import timber.log.Timber
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.UnknownHostException
import java.util.concurrent.atomic.AtomicBoolean

class TcpClientService : Service() {

    @Throws(IOException::class)
    private fun buildSocket(): Socket {
        val socket = Socket()
        socket.soTimeout = TCP_SERVICE_TCP_TIMEOUT_INT
        socket.connect(
            InetSocketAddress(
                resolveHost(ip, TCP_SERVICE_DNS_TIMEOUT),
                TCP_SERVICE_PORT
            ), TCP_SERVICE_TCP_TIMEOUT_INT
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

    init {
        instance = this
    }

    companion object {
        lateinit var instance: TcpClientService

        fun terminateService() {
            instance.stopSelf()
        }
    }

    private val working = AtomicBoolean(true)
    private lateinit var socket: Socket

    private var dataOutputStream: DataOutputStream? = null
    private var message = ""
    private var ip = ""

    private val runnable: Runnable = Runnable {
        try {
            Timber.d("Runnable")
            socket = buildSocket()
            dataOutputStream = DataOutputStream(socket.getOutputStream())
            while (working.get()) {
                try {
                    dataOutputStream!!.writeUTF(message)
                    Thread.sleep(TCP_SERVICE_THREAD_TIMEOUT)
                    dataOutputStream!!.close()
                    working.set(false)
                    terminateService()
                } catch (e: IOException) {
                    e.printStackTrace()
                    try {
                        dataOutputStream!!.close()
                        terminateService()
                    } catch (ex: IOException) {
                        ex.printStackTrace()
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    try {
                        dataOutputStream!!.close()
                        terminateService()
                    } catch (ex: IOException) {
                        ex.printStackTrace()
                    }
                }
            }
            terminateService()
        } catch (e: IOException) {
            Timber.d("Runnable")
            terminateService()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand")
        message = intent?.extras?.getString("payload").toString()
        ip = intent?.extras?.getString("ip").toString()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        Timber.d("onCreate")
        startMeForeground()
        Thread(runnable).start()
    }

    override fun onDestroy() {
        Timber.d("OnDestroy")
        working.set(false)
    }

    private fun startMeForeground() {
        Timber.d("startMeForeground")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannelId = packageName
            val channelName = "Tcp Client Background Service"
            val chan = NotificationChannel(
                notificationChannelId,
                channelName,
                NotificationManager.IMPORTANCE_NONE
            )
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val manager = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            manager.createNotificationChannel(chan)
            val notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
            val notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Tcp Client is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()
            startForeground(2, notification)
        } else {
            startForeground(1, Notification())
        }
    }
}