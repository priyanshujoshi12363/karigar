package com.karigar.worker.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build

const val JOBS_CHANNEL_ID = "karigar_jobs"

val JOBS_VIBRATION_PATTERN = longArrayOf(0, 400, 200, 400, 200, 400)

object NotificationChannels {
    fun ensure(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(JOBS_CHANNEL_ID) != null) return

        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val channel = NotificationChannel(
            JOBS_CHANNEL_ID,
            "Job requests",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "New work requests and order updates"
            enableVibration(true)
            vibrationPattern = JOBS_VIBRATION_PATTERN
            enableLights(true)
            setSound(sound, attrs)
            setBypassDnd(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        manager.createNotificationChannel(channel)
    }
}
