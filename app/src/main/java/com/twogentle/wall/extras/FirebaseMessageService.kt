package com.twogentle.wall.extras

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.twogentle.wall.R

class FirebaseMessageService : FirebaseMessagingService() {

    companion object {
        const val NOTIFICATION_ID = 201
        const val CHANNEL_ID = "general"
        const val CHANNEL_NAME = "General"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                )
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
        builder.setContentTitle(remoteMessage.notification!!.title)
            .setContentText(remoteMessage.notification!!.body)
            .setStyle(NotificationCompat.BigTextStyle())
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setSmallIcon(R.drawable.ic_stat_notify)
            .setAutoCancel(true)
            .setChannelId(CHANNEL_ID)
            .priority = 8

        val notification = builder.build()

        notificationManager.notify(NOTIFICATION_ID, notification)

    }
}