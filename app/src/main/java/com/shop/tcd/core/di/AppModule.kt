package com.shop.tcd.core.di

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import com.shop.tcd.R
import com.shop.tcd.core.utils.Constants.Notifications.CHANNEL_ID
import com.shop.tcd.core.utils.Constants.Notifications.CHANNEL_NAME
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(private val application: Application) {

    @Provides
    @Singleton
    fun providesApplicationContext(): Context = application

    @RequiresApi(Build.VERSION_CODES.O)
    @Provides
    fun providesNotificationBuilder(context: Context): Notification.Builder =
        Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setOngoing(false)

    @Provides
    @Singleton
    fun providesNotificationManager(
        context: Context,
        notificationChannel: NotificationChannel
    ): NotificationManager =
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
            createNotificationChannel(notificationChannel)
        }

    @Provides
    @Singleton
    fun providesNotificationChannel(
        context: Context
    ): NotificationChannel {
        val soundUri =
            Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.notification_sound}")
        return NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Уведомление об обновлениях"
            setBypassDnd(true)
            canBypassDnd()
            setSound(
                soundUri,
                audioAttributes
            )
        }
    }
}
