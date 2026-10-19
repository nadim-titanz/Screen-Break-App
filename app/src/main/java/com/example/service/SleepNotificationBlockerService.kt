package com.example.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.example.ScreenBreakApplication
import com.example.data.SleepScheduleEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Intercepts and hides notifications posted by apps configured in an active Sleep Schedule.
 * Notifications are snoozed until the schedule's end time, at which point the Android OS
 * automatically re-delivers and reveals them to the user.
 */
class SleepNotificationBlockerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var cachedSchedules: List<SleepScheduleEntity> = emptyList()

    override fun onCreate() {
        super.onCreate()
        instance = this
        observeSchedules()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        Log.i(TAG, "Notification Listener connected successfully")
        _isListenerConnected.value = true
        sweepActiveNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        _isListenerConnected.value = false
        if (instance == this) {
            instance = null
        }
        Log.i(TAG, "Notification Listener disconnected")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        _isListenerConnected.value = false
        if (instance == this) {
            instance = null
        }
    }

    private fun observeSchedules() {
        serviceScope.launch {
            val app = applicationContext as? ScreenBreakApplication ?: return@launch
            app.repository.sleepSchedules.collect { schedules ->
                cachedSchedules = schedules.filter { it.isEnabled }
                sweepActiveNotifications()
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val pkg = sbn.packageName ?: return
        if (pkg.isEmpty() || pkg == packageName) return
        if (sbn.isOngoing) return

        // Respect temporary 5-min emergency pass if active
        val currentUsageState = ContinuousUsageService.usageState.value
        if (currentUsageState.emergencySnoozeSecondsRemaining > 0 &&
            (currentUsageState.emergencySnoozedPackage == pkg || currentUsageState.emergencySnoozedPackage.isEmpty())
        ) {
            return
        }

        val now = Calendar.getInstance()
        val activeSchedule = cachedSchedules.firstOrNull { it.isActiveAt(now) && it.isAppBlocked(pkg) }
        if (activeSchedule != null) {
            val remainingDurationMs = activeSchedule.getRemainingActiveDurationMs(now)
            if (remainingDurationMs > 0) {
                hideNotification(sbn, remainingDurationMs, pkg, activeSchedule.title)
            }
        }
    }

    fun sweepActiveNotifications() {
        val now = Calendar.getInstance()
        val activeSchedule = cachedSchedules.firstOrNull { it.isActiveAt(now) } ?: return
        val blockedPackages = activeSchedule.getBlockedPackageSet()
        if (blockedPackages.isEmpty()) return

        val remainingDurationMs = activeSchedule.getRemainingActiveDurationMs(now)
        if (remainingDurationMs <= 0) return

        val currentUsageState = ContinuousUsageService.usageState.value
        val emergencyPkg = if (currentUsageState.emergencySnoozeSecondsRemaining > 0) {
            currentUsageState.emergencySnoozedPackage
        } else {
            ""
        }

        val activeNotifications = try {
            getActiveNotifications()
        } catch (e: Exception) {
            Log.w(TAG, "Unable to get active notifications", e)
            null
        } ?: return

        for (sbn in activeNotifications) {
            val pkg = sbn.packageName ?: continue
            if (pkg == packageName || sbn.isOngoing) continue
            if (emergencyPkg.isNotEmpty() && emergencyPkg == pkg) continue

            if (blockedPackages.contains(pkg)) {
                hideNotification(sbn, remainingDurationMs, pkg, activeSchedule.title)
            }
        }
    }

    private fun hideNotification(
        sbn: StatusBarNotification,
        durationMs: Long,
        pkg: String,
        scheduleTitle: String
    ) {
        var hidden = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                snoozeNotification(sbn.key, durationMs)
                hidden = true
                Log.d(TAG, "Snoozed notification from $pkg for ${durationMs / 1000}s until $scheduleTitle ends")
            } catch (e: Exception) {
                Log.w(TAG, "snoozeNotification failed, falling back to cancelNotification", e)
            }
        }

        if (!hidden) {
            try {
                cancelNotification(sbn.key)
                hidden = true
                Log.d(TAG, "Cancelled notification from $pkg for active schedule $scheduleTitle")
            } catch (e: Exception) {
                Log.w(TAG, "cancelNotification failed for $pkg", e)
            }
        }

        if (hidden) {
            _hiddenNotificationsCount.value += 1
        }
    }

    companion object {
        private const val TAG = "SleepNotificationBlocker"

        @Volatile
        var instance: SleepNotificationBlockerService? = null
            private set

        private val _isListenerConnected = MutableStateFlow(false)
        val isListenerConnected: StateFlow<Boolean> = _isListenerConnected.asStateFlow()

        private val _hiddenNotificationsCount = MutableStateFlow(0)
        val hiddenNotificationsCount: StateFlow<Int> = _hiddenNotificationsCount.asStateFlow()

        fun resetHiddenCount() {
            _hiddenNotificationsCount.value = 0
        }

        fun sweepActiveBlockedNotifications(context: Context) {
            instance?.sweepActiveNotifications()
        }

        /**
         * Checks whether the user has granted notification listener access to this app.
         */
        fun hasNotificationAccess(context: Context): Boolean {
            return try {
                val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context)
                if (enabledPackages.contains(context.packageName)) {
                    return true
                }
                val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
                if (!TextUtils.isEmpty(flat)) {
                    flat.split(":").any { componentNameString ->
                        val cn = ComponentName.unflattenFromString(componentNameString)
                        cn != null && TextUtils.equals(context.packageName, cn.packageName)
                    }
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }

        /**
         * Returns an Intent to open the Android Notification Listener Settings page.
         */
        fun getNotificationAccessIntent(context: Context): Intent {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val detailIntent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).apply {
                    putExtra(
                        Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
                        ComponentName(context, SleepNotificationBlockerService::class.java).flattenToString()
                    )
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                if (detailIntent.resolveActivity(context.packageManager) != null) {
                    return detailIntent
                }
            }
            return Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
    }
}
