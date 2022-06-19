package com.shop.tcd.core.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import com.shop.tcd.R
import com.shop.tcd.core.utils.Constants.Notifications.NOTY_ID
import javax.inject.Inject

class ServiceNotification @Inject constructor(
    private var context: Context,
    private var notificationBuilder: Notification.Builder,
    private var manager: NotificationManager,
) {

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setNotificationText(title: String, text: String): Notification.Builder =
        notificationBuilder
            .setContentTitle(title)
            .setContentText(text)

    @RequiresApi(Build.VERSION_CODES.O)
    fun showNotification(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        val pendingIntent =
            PendingIntent.getActivity(context, 0, intent, 0)
        notificationBuilder = setNotificationText(
            title = context.getString(R.string.notification_title),
            text = context.getString(R.string.notification_text)
        )
            .setContentIntent(pendingIntent)
        manager.notify(NOTY_ID, notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(
        channelId: String,
        channelName: String
    ) {
        val soundUri =
            Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.notification_sound}")

        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_MAX)
                .apply {
                    description = "Уведомление об обновлениях"
                    setBypassDnd(true)
                    canBypassDnd()
                    setSound(
                        soundUri,
                        audioAttributes
                    )
                }
        manager.createNotificationChannel(channel)
    }
}
