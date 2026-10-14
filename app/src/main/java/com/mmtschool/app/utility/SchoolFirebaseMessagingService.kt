package com.mmtschool.app.utility

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mmtschool.app.R
import com.mmtschool.app.data.UserPrefsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Register in AndroidManifest.xml:
 * <service
 *     android:name=".utility.SchoolFirebaseMessagingService"
 *     android:exported="false">
 *     <intent-filter>
 *         <action android:name="com.google.firebase.MESSAGING_EVENT" />
 *     </intent-filter>
 * </service>
 */


class SchoolFirebaseMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val notificationManager = AttendanceNotificationManager()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        scope.launch {
            val session = UserPrefsRepository(applicationContext).getSessionOnce() ?: return@launch
            val username = session.rollNumber ?: return@launch
            notificationManager.refreshToken(session.schoolId.toString(), username, token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title
        val body = message.notification?.body
        val channelId = message.notification?.channelId ?: NotificationChannels.ENTRY_ALERTS_CHANNEL_ID

        if (title != null && body != null) {
            showForegroundNotification(title, body, channelId)
        }
    }

    private fun showForegroundNotification(title: String, body: String, channelId: String) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(Random.nextInt(), notification)
    }
}