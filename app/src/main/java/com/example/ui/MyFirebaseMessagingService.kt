package com.example.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.MainActivity

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "إشعار جديد"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: ""
        val targetRoute = remoteMessage.data["route"] // e.g. "notifications"

        sendNotification(title, body, targetRoute)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Usually, we'd send this to our backend to associate with the current user.
        // For this demo, we'll just log it or save locally.
        println("FCM Token: \$token")
    }

    private fun sendNotification(title: String, body: String, targetRoute: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            if (targetRoute != null) {
                putExtra("target_route", targetRoute)
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "kayan_admin_notifications"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            // Need a valid icon here, usually R.drawable.ic_launcher_foreground
            // Or android.R.drawable.ic_dialog_info if none exist
            .setSmallIcon(android.R.drawable.ic_dialog_info) 
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "إشعارات النظام",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}
