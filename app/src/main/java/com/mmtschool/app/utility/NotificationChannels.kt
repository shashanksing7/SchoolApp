package com.mmtschool.app.utility


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import com.mmtschool.app.R

object NotificationChannels {

    const val ENTRY_ALERTS_CHANNEL_ID = "entry_alerts"
    const val EXIT_ALERTS_CHANNEL_ID = "exit_alerts"

    /** Must run before any notification can arrive, so call from Application.onCreate(). */
    fun create(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = ContextCompat.getSystemService(context, NotificationManager::class.java) ?: return

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val entrySound = Uri.parse("android.resource://${context.packageName}/${R.raw.entry}")
        val exitSound = Uri.parse("android.resource://${context.packageName}/${R.raw.exit}")

        manager.createNotificationChannel(
            NotificationChannel(ENTRY_ALERTS_CHANNEL_ID, "Entry Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifies when a student enters school premises"
                setSound(entrySound, audioAttributes)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(EXIT_ALERTS_CHANNEL_ID, "Exit Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifies when a student exits school premises"
                setSound(exitSound, audioAttributes)
            }
        )
    }
}