package com.example.restopass.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.restopass.R
import com.example.restopass.common.AppPreferences
import com.example.restopass.main.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

enum class NotificationType {
    INVITE_RESERVATION,
    CANCEL_RESERVATION,
    CONFIRMED_RESERVATION,
    SCORE_EXPERIENCE
}

class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        showNotification(remoteMessage)
    }

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        AppPreferences.firebaseToken = newToken
    }

    private fun showNotification(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data

        val intent = Intent(this, MainActivity::class.java)
        intent.apply {
            putExtra("fcmNotification", "true")
            putExtra("notificationType", data["type"])
            putExtra("reservationId", data["reservation_id"])
            if(data["type"].equals(NotificationType.SCORE_EXPERIENCE.name))
                putExtra("restaurantId", data["restaurantId"])
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.restopass_titless)
            .setColor(ContextCompat.getColor(this, R.color.restoPassGreen))
            .setContentTitle(data["title"])
            .setContentText(data["description"])
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Reservas",
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    companion object {
        private const val CHANNEL_ID = "CHANNEL_1"
    }
}