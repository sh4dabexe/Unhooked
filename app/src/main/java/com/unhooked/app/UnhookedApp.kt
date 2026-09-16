package com.unhooked.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.unhooked.app.data.db.AppDatabase
import com.unhooked.app.data.preferences.UserPreferencesDataStore
import com.unhooked.app.data.repository.UnhookedRepository

class UnhookedApp : Application() {

    companion object {
        lateinit var instance: UnhookedApp
            private set

        var repository: UnhookedRepository? = null
            private set

        const val CHANNEL_FOCUS_ID = "unhooked_focus_channel"
        const val CHANNEL_ALERTS_ID = "unhooked_alerts_channel"
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        val db = AppDatabase.getInstance(this)
        val prefs = UserPreferencesDataStore(this)
        repository = UnhookedRepository(db, prefs)

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusChannel = NotificationChannel(
                CHANNEL_FOCUS_ID,
                "Focus Sessions",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows notifications for ongoing and completed focus sessions"
            }

            val alertsChannel = NotificationChannel(
                CHANNEL_ALERTS_ID,
                "Limit Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when daily limits or schedules are reached"
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(focusChannel)
            manager.createNotificationChannel(alertsChannel)
        }
    }
}
