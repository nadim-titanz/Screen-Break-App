package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.ScreenBreakApplication
import com.example.data.SleepScheduleEntity
import com.example.model.UsageState
import com.example.ui.ReminderAlertActivity
import com.example.ui.SleepBlockerActivity
import java.util.Calendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ContinuousUsageService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var tickerJob: Job? = null

    private lateinit var overlayManager: FloatingOverlayManager
    private var isScreenOn = true
    private var continuousSeconds = 0L
    private var snoozeUntilSeconds = -1L
    private var lockedPackage: String = ""
    private var lockedAppName: String = ""
    private var lockedUntilTimestamp: Long = 0L
    private var lastBlockLogTime: Long = 0L

    private var cachedSchedules: List<SleepScheduleEntity> = emptyList()
    private var cachedIgnoredPackages: Set<String> = emptySet()
    private var emergencySnoozeUntilTimestamp: Long = 0L
    private var emergencySnoozedPackage: String = ""
    private var lastSleepBlockAlertTime: Long = 0L
    private var lastSleepBlockLogTime: Long = 0L

    fun isPackageIgnored(pkg: String): Boolean {
        if (pkg.isEmpty()) return false
        if (cachedIgnoredPackages.contains(pkg)) return true
        return cachedIgnoredPackages.any { it.equals(pkg, ignoreCase = true) }
    }

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    isScreenOn = false
                    // Screen turned off - continuous usage resets!
                    continuousSeconds = 0L
                    snoozeUntilSeconds = -1L
                    updateNotification()
                    updateState()
                }
                Intent.ACTION_SCREEN_ON,
                Intent.ACTION_USER_PRESENT -> {
                    isScreenOn = true
                    updateNotification()
                    updateState()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val app = application as ScreenBreakApplication
        val repository = app.repository

        overlayManager = FloatingOverlayManager(
            context = this,
            onDismissCallback = { pkg, name ->
                handleTakeBreak(pkg, name)
            },
            onSnoozeCallback = {
                snooze5Minutes()
            },
            onIgnoreAppCallback = { pkg, name ->
                serviceScope.launch {
                    repository.setAppIgnored(pkg, name, true)
                    repository.logAction(
                        (continuousSeconds / 60).toInt().coerceAtLeast(1),
                        pkg,
                        name,
                        "ADDED_TO_IGNORED"
                    )
                    updateState()
                }
            }
        )

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(screenReceiver, filter)

        // Observe sleep schedules reactively
        serviceScope.launch {
            repository.sleepSchedules.collect { schedules ->
                cachedSchedules = schedules
                updateState()
            }
        }

        // Observe ignored apps reactively
        serviceScope.launch {
            repository.ignoredApps.collect { ignoredList ->
                cachedIgnoredPackages = ignoredList.filter { it.isIgnored }.map { it.packageName }.toSet()
                val currentFg = _usageStateFlow.value.activePackage
                if (currentFg.isNotEmpty()) {
                    _usageStateFlow.value = _usageStateFlow.value.copy(
                        isCurrentAppIgnored = isPackageIgnored(currentFg)
                    )
                }
            }
        }

        startForeground(NOTIFICATION_ID, buildOngoingNotification())
        startTimer()
        _usageStateFlow.value = _usageStateFlow.value.copy(isServiceRunning = true)
    }

    private fun startTimer() {
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            val app = application as ScreenBreakApplication
            val repository = app.repository

            var tickCounter = 0

            while (isActive) {
                delay(500)
                tickCounter++

                val now = System.currentTimeMillis()

                // 1. Check emergency snooze expiration
                if (emergencySnoozeUntilTimestamp != 0L && now >= emergencySnoozeUntilTimestamp) {
                    emergencySnoozeUntilTimestamp = 0L
                    val expiredPkg = emergencySnoozedPackage
                    emergencySnoozedPackage = ""
                    showToast("⛔ 5-Minute emergency use expired. Sleep schedule re-enforced!")
                    val (currentFgPkg, _) = CurrentAppDetector.getForegroundApp(this@ContinuousUsageService)
                    if (currentFgPkg.isNotEmpty() && currentFgPkg == expiredPkg) {
                        kickToHomeScreen(this@ContinuousUsageService)
                    }
                    updateState()
                }

                // 2. Sleep Schedule Blocker Enforcement
                val currentCalendar = Calendar.getInstance()
                val activeSleepSchedule = cachedSchedules.firstOrNull { it.isActiveAt(currentCalendar) }
                if (activeSleepSchedule != null) {
                    val blockedPackages = activeSleepSchedule.getBlockedPackageSet()
                    if (blockedPackages.isNotEmpty()) {
                        val (currentFgPkg, currentFgName) = CurrentAppDetector.getForegroundApp(this@ContinuousUsageService)
                        if (currentFgPkg.isNotEmpty() && currentFgPkg != packageName && blockedPackages.contains(currentFgPkg)) {
                            val isEmergencySnoozed = emergencySnoozeUntilTimestamp > now &&
                                (emergencySnoozedPackage == currentFgPkg || emergencySnoozedPackage.isEmpty())
                            if (!isEmergencySnoozed) {
                                // Auto-kick to Home and show SleepBlockerActivity!
                                kickToHomeScreen(this@ContinuousUsageService)
                                if (now - lastSleepBlockAlertTime > 2500L) {
                                    lastSleepBlockAlertTime = now
                                    SleepBlockerActivity.start(
                                        this@ContinuousUsageService,
                                        scheduleTitle = activeSleepSchedule.title,
                                        blockedPackage = currentFgPkg,
                                        appName = currentFgName.ifEmpty { currentFgPkg },
                                        timeRange = activeSleepSchedule.formattedTimeRange()
                                    )
                                }
                                if (now - lastSleepBlockLogTime > 4000L) {
                                    lastSleepBlockLogTime = now
                                    serviceScope.launch {
                                        repository.logAction(
                                            0,
                                            currentFgPkg,
                                            currentFgName,
                                            "SLEEP_SCHEDULE_BLOCKED"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Check active 30s lockout enforcement across all apps
                if (lockedUntilTimestamp > now) {
                    val (currentFgPkg, currentFgName) = CurrentAppDetector.getForegroundApp(this@ContinuousUsageService)
                    if (currentFgPkg.isNotEmpty()) {
                        val isExempt = CurrentAppDetector.isExemptFromLockout(this@ContinuousUsageService, currentFgPkg)
                        if (!isExempt) {
                            val remainingSeconds = ((lockedUntilTimestamp - now + 999) / 1000).toInt().coerceAtLeast(1)
                            kickToHomeScreen(this@ContinuousUsageService)

                            if (now - lastBlockLogTime > 2500L) {
                                lastBlockLogTime = now
                                val blockedTitle = if (lockedPackage.isNotEmpty() && currentFgPkg == lockedPackage) {
                                    lockedAppName.ifEmpty { currentFgName.ifEmpty { "App" } }
                                } else {
                                    currentFgName.ifEmpty { CurrentAppDetector.getAppName(this@ContinuousUsageService, currentFgPkg) }.ifEmpty { "App" }
                                }
                                showToast("🔒 $blockedTitle is locked! (${remainingSeconds}s break left)")

                                serviceScope.launch {
                                    repository.logAction(
                                        0,
                                        currentFgPkg,
                                        currentFgName,
                                        "LOCKED_APP_ACCESS_BLOCKED"
                                    )
                                }
                            }
                        }
                    }
                } else if (lockedUntilTimestamp != 0L && now >= lockedUntilTimestamp) {
                    val unlockedName = lockedAppName.ifEmpty { "Apps" }
                    showToast("✅ Screen break complete. $unlockedName is unlocked.")
                    lockedPackage = ""
                    lockedAppName = ""
                    lockedUntilTimestamp = 0L
                    updateState()
                }

                if (isScreenOn) {
                    // Update continuous seconds every 1000ms (every 2 half-second ticks)
                    if (tickCounter % 2 == 0) {
                        continuousSeconds++

                        // Check foreground app every second for fast, accurate updates
                        val (pkg, name) = CurrentAppDetector.getForegroundApp(this@ContinuousUsageService)
                        val isIgnored = if (pkg.isNotEmpty()) isPackageIgnored(pkg) else false

                        _usageStateFlow.value = _usageStateFlow.value.copy(
                            activePackage = pkg,
                            activeAppName = name,
                            isCurrentAppIgnored = isIgnored
                        )

                        // If user moved to an ignored app while overlay is showing, dismiss the overlay
                        if (isIgnored && overlayManager.isShowing) {
                            overlayManager.dismiss()
                        }

                        val settings = repository.settings.value
                        val intervalSeconds = settings.intervalMinutes * 60

                        // Check if interval reached and not currently snoozed
                        if (snoozeUntilSeconds > 0 && continuousSeconds >= snoozeUntilSeconds) {
                            snoozeUntilSeconds = -1L
                        }

                        if (continuousSeconds > 0 &&
                            continuousSeconds % intervalSeconds == 0L &&
                            snoozeUntilSeconds <= 0
                        ) {
                            val currentPkg = if (pkg.isNotEmpty()) pkg else _usageStateFlow.value.activePackage
                            val currentName = if (name.isNotEmpty()) name else _usageStateFlow.value.activeAppName
                            val isCurrentIgnored = isIgnored || (currentPkg.isNotEmpty() && (isPackageIgnored(currentPkg) || repository.isAppIgnored(currentPkg)))

                            if (isCurrentIgnored) {
                                // Suppress reminder because app is ignored!
                                repository.logAction(
                                    settings.intervalMinutes,
                                    currentPkg,
                                    currentName,
                                    "REMINDER_SUPPRESSED"
                                )
                            } else {
                                // Trigger Pop-up Reminder!
                                triggerReminder(
                                    continuousMinutes = (continuousSeconds / 60).toInt(),
                                    appName = currentName,
                                    packageName = currentPkg
                                )
                            }
                        }

                        // Update notification every 10 seconds to avoid spamming system
                        if (continuousSeconds % 10 == 0L) {
                            updateNotification()
                        }
                    }
                }

                updateState()
            }
        }
    }

    private fun triggerReminder(continuousMinutes: Int, appName: String, packageName: String) {
        // Double safeguard: Never show pop-up for ignored apps
        if (packageName.isNotEmpty() && isPackageIgnored(packageName)) {
            return
        }
        val (fgPkg, _) = CurrentAppDetector.getForegroundApp(this)
        if (fgPkg.isNotEmpty() && isPackageIgnored(fgPkg)) {
            return
        }

        val app = application as ScreenBreakApplication
        val repository = app.repository
        val settings = repository.settings.value

        serviceScope.launch {
            repository.logAction(continuousMinutes, packageName, appName, "REMINDER_SHOWN")
        }

        // 1. Show Floating Overlay Window if permission granted
        if (settings.overlayEnabled && CurrentAppDetector.hasOverlayPermission(this)) {
            overlayManager.show(continuousMinutes, appName, packageName)
        }

        // 2. Also trigger High-Priority Heads-Up Alert Notification
        showHighPriorityAlertNotification(continuousMinutes, appName, packageName)

        // 3. Vibrate if enabled
        if (settings.vibrationEnabled) {
            triggerVibration()
        }

        _usageStateFlow.value = _usageStateFlow.value.copy(
            lastReminderTime = System.currentTimeMillis()
        )
    }

    @SuppressLint("MissingPermission")
    private fun triggerVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 300, 200, 300), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 300, 200, 300), -1)
            }
        } catch (e: Exception) {
            // Ignore vibration failure
        }
    }

    private fun showHighPriorityAlertNotification(
        minutes: Int,
        appName: String,
        packageName: String
    ) {
        val fullScreenIntent = ReminderAlertActivity.createIntent(this, minutes, appName, packageName)
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            1001,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val resetIntent = Intent(this, ContinuousUsageService::class.java).apply {
            action = ACTION_RESET
            putExtra(EXTRA_LOCKED_PACKAGE, packageName)
            putExtra(EXTRA_LOCKED_APP_NAME, appName)
        }
        val resetPendingIntent = PendingIntent.getService(
            this,
            1003,
            resetIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val appDetail = if (appName.isNotEmpty()) " while using $appName" else ""

        val notification = NotificationCompat.Builder(this, ScreenBreakApplication.CHANNEL_ALERT_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Interrupt Reminder ⏱️")
            .setContentText("You've been continuously using your phone for $minutes minutes$appDetail.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "You've been continuously using your phone for $minutes minutes$appDetail. Strict Detox: time to exit screen and reset!"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .addAction(0, "Taking Break", resetPendingIntent)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(ALERT_NOTIFICATION_ID, notification)
    }

    private fun buildOngoingNotification(): Notification {
        val app = application as ScreenBreakApplication
        val intervalMinutes = app.repository.settings.value.intervalMinutes

        val minutes = continuousSeconds / 60
        val seconds = continuousSeconds % 60
        val timeFormatted = "%02d:%02d".format(minutes, seconds)
        val targetFormatted = "%02d:00".format(intervalMinutes)

        val activeApp = _usageStateFlow.value.activeAppName
        val isIgnored = _usageStateFlow.value.isCurrentAppIgnored

        val statusText = when {
            !isScreenOn -> "Screen is off • Paused"
            isIgnored -> "Active: $activeApp (Ignored - No Pop-up)"
            activeApp.isNotEmpty() -> "Continuous: $timeFormatted / $targetFormatted • $activeApp"
            else -> "Continuous: $timeFormatted / $targetFormatted"
        }

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, ScreenBreakApplication.CHANNEL_SERVICE_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Interrupt Monitor")
            .setContentText(statusText)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, buildOngoingNotification())
    }

    private fun showToast(message: String) {
        serviceScope.launch(Dispatchers.Main) {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateState() {
        val app = application as ScreenBreakApplication
        val intervalMinutes = app.repository.settings.value.intervalMinutes
        val now = System.currentTimeMillis()
        val remainingLocked = if (lockedUntilTimestamp > now) {
            ((lockedUntilTimestamp - now + 999) / 1000).toInt()
        } else {
            0
        }

        val remainingEmergencySnooze = if (emergencySnoozeUntilTimestamp > now) {
            ((emergencySnoozeUntilTimestamp - now + 999) / 1000).toInt()
        } else {
            0
        }

        val currentCalendar = Calendar.getInstance()
        val activeSleepSchedule = cachedSchedules.firstOrNull { it.isActiveAt(currentCalendar) }

        _usageStateFlow.value = _usageStateFlow.value.copy(
            continuousSeconds = continuousSeconds,
            intervalMinutes = intervalMinutes,
            isScreenOn = isScreenOn,
            isServiceRunning = true,
            lockedPackage = if (remainingLocked > 0) lockedPackage else "",
            lockedAppName = if (remainingLocked > 0) lockedAppName else "",
            lockedSecondsRemaining = remainingLocked,
            isSleepScheduleActive = activeSleepSchedule != null,
            activeSleepScheduleTitle = activeSleepSchedule?.title ?: "",
            emergencySnoozeSecondsRemaining = remainingEmergencySnooze,
            emergencySnoozedPackage = if (remainingEmergencySnooze > 0) emergencySnoozedPackage else ""
        )
    }

    private fun snooze5Minutes() {
        snoozeUntilSeconds = continuousSeconds + 5 * 60
        overlayManager.dismiss()
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancel(ALERT_NOTIFICATION_ID)
    }

    private fun handleTakeBreak(targetPackage: String = "", targetAppName: String = "") {
        // Strict Dopamine Detox: Auto kick out from the running app to Home Screen!
        kickToHomeScreen(this)

        val app = application as ScreenBreakApplication
        val (freshFgPkg, freshFgName) = CurrentAppDetector.getForegroundApp(this)

        val currentPkg = when {
            targetPackage.isNotEmpty() && targetPackage != packageName -> targetPackage
            freshFgPkg.isNotEmpty() && freshFgPkg != packageName -> freshFgPkg
            _usageStateFlow.value.activePackage.isNotEmpty() && _usageStateFlow.value.activePackage != packageName -> _usageStateFlow.value.activePackage
            else -> ""
        }
        val currentName = when {
            targetAppName.isNotEmpty() -> targetAppName
            freshFgName.isNotEmpty() -> freshFgName
            _usageStateFlow.value.activeAppName.isNotEmpty() -> _usageStateFlow.value.activeAppName
            currentPkg.isNotEmpty() -> CurrentAppDetector.getAppName(this, currentPkg)
            else -> ""
        }
        val breakMinutes = (continuousSeconds / 60).toInt().coerceAtLeast(1)

        // Always lock for 30 seconds (Strict Detox) across all apps
        lockedUntilTimestamp = System.currentTimeMillis() + 30_000L
        lockedPackage = currentPkg
        lockedAppName = currentName.ifEmpty { "Apps" }

        if (currentPkg.isNotEmpty()) {
            showToast("🔒 $lockedAppName is locked for 30s (Strict Detox)")
        } else {
            showToast("🔒 Screen break started: apps locked for 30s")
        }

        serviceScope.launch {
            app.repository.logAction(
                breakMinutes,
                currentPkg,
                currentName,
                "BREAK_TAKEN"
            )
        }

        continuousSeconds = 0L
        snoozeUntilSeconds = -1L
        overlayManager.dismiss()
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancel(ALERT_NOTIFICATION_ID)
        updateNotification()
        updateState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
            }
            ACTION_RESET -> {
                val pkg = intent?.getStringExtra(EXTRA_LOCKED_PACKAGE) ?: ""
                val name = intent?.getStringExtra(EXTRA_LOCKED_APP_NAME) ?: ""
                handleTakeBreak(pkg, name)
            }
            ACTION_SNOOZE -> {
                snooze5Minutes()
            }
            ACTION_TEST_REMINDER -> {
                val currentPkg = _usageStateFlow.value.activePackage
                val currentName = _usageStateFlow.value.activeAppName
                val testMinutes = (continuousSeconds / 60).toInt().coerceAtLeast(10)
                triggerReminder(testMinutes, currentName, currentPkg)
            }
            ACTION_DISMISS_POPUP -> {
                overlayManager.dismiss()
            }
            ACTION_EMERGENCY_SNOOZE -> {
                val pkg = intent?.getStringExtra(EXTRA_EMERGENCY_PACKAGE) ?: ""
                emergencySnoozedPackage = pkg
                // Exactly 5-Minute emergency use allowed!
                emergencySnoozeUntilTimestamp = System.currentTimeMillis() + 5 * 60 * 1000L
                showToast("⏳ 5-Minute emergency use started. Sleep blocker resumes after 5m.")
                updateState()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        tickerJob?.cancel()
        serviceScope.cancel()
        overlayManager.dismiss()
        try {
            unregisterReceiver(screenReceiver)
        } catch (e: Exception) {
            // Unregister failed or wasn't registered
        }
        _usageStateFlow.value = _usageStateFlow.value.copy(isServiceRunning = false)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val NOTIFICATION_ID = 2001
        const val ALERT_NOTIFICATION_ID = 2002

        const val ACTION_START = "com.example.action.START"
        const val ACTION_STOP = "com.example.action.STOP"
        const val ACTION_RESET = "com.example.action.RESET"
        const val ACTION_SNOOZE = "com.example.action.SNOOZE"
        const val ACTION_TEST_REMINDER = "com.example.action.TEST_REMINDER"
        const val ACTION_DISMISS_POPUP = "com.example.action.DISMISS_POPUP"
        const val ACTION_EMERGENCY_SNOOZE = "com.example.action.EMERGENCY_SNOOZE"

        const val EXTRA_LOCKED_PACKAGE = "com.example.extra.LOCKED_PACKAGE"
        const val EXTRA_LOCKED_APP_NAME = "com.example.extra.LOCKED_APP_NAME"
        const val EXTRA_EMERGENCY_PACKAGE = "com.example.extra.EMERGENCY_PACKAGE"

        private val _usageStateFlow = MutableStateFlow(UsageState())
        val usageState: StateFlow<UsageState> = _usageStateFlow.asStateFlow()

        fun startService(context: Context) {
            val intent = Intent(context, ContinuousUsageService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, ContinuousUsageService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun sendAction(context: Context, action: String) {
            val intent = Intent(context, ContinuousUsageService::class.java).apply {
                this.action = action
            }
            context.startService(intent)
        }

        fun kickToHomeScreen(context: Context) {
            try {
                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(homeIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun startEmergencySnooze(context: Context, packageName: String) {
            val intent = Intent(context, ContinuousUsageService::class.java).apply {
                action = ACTION_EMERGENCY_SNOOZE
                putExtra(EXTRA_EMERGENCY_PACKAGE, packageName)
            }
            context.startService(intent)
        }
    }
}
