package com.karigar.worker.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.karigar.worker.MainActivity
import com.karigar.worker.R
import com.karigar.worker.data.TokenStore
import com.karigar.worker.data.remote.ApiClient
import com.karigar.worker.data.remote.PushTokenRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WorkerMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        val jwt = TokenStore(this).getToken() ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ApiClient.api.savePushToken("Bearer $jwt", PushTokenRequest(token))
            } catch (_: Exception) {
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: "Karigar"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        NotificationChannels.ensure(this)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pending = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(this, JOBS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVibrate(JOBS_VIBRATION_PATTERN)
            .setSound(sound)
            .setContentIntent(pending)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
