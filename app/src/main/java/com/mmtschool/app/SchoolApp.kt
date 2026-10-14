package com.mmtschool.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.mmtschool.app.utility.NotificationChannels

/**
 * Application class - makes sure Firebase is initialized and the
 * entry_alerts / exit_alerts notification channels exist before
 * anything tries to talk to Firestore or send a notification.
 */
class SchoolApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        NotificationChannels.create(this)
    }
}