package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.data.AppDatabase
import com.example.data.ReminderRepository

class ScreenBreakApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: ReminderRepository by lazy {
        ReminderRepository(
            database.ignoredAppDao(),
            database.usageLogDao(),
            database.sleepScheduleDao(),
            this
        )
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Service ongoing channel
            val serviceChannel = NotificationChannel(
                CHANNEL_SERVICE_ID,
                "Interrupt Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows continuous screen-on time"
                setShowBadge(false)
            }

            // Alert Pop-up reminder channel
            val alertChannel = NotificationChannel(
                CHANNEL_ALERT_ID,
                "Interrupt Pop-up Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Pop-up reminders when continuous phone usage reaches limit"
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(serviceChannel)
            notificationManager.createNotificationChannel(alertChannel)
        }
    }

    companion object {
        const val CHANNEL_SERVICE_ID = "screen_break_service_channel"
        const val CHANNEL_ALERT_ID = "screen_break_alert_channel"
    }
}
