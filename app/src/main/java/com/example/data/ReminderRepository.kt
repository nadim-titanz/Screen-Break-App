package com.example.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.example.model.AppItem
import com.example.model.ReminderSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ReminderRepository(
    private val ignoredAppDao: IgnoredAppDao,
    private val usageLogDao: UsageLogDao,
    private val sleepScheduleDao: SleepScheduleDao,
    private val context: Context
) {
    private val prefs = context.getSharedPreferences("screen_break_prefs", Context.MODE_PRIVATE)

    val ignoredApps: Flow<List<IgnoredAppEntity>> = ignoredAppDao.getIgnoredApps()
    val recentLogs: Flow<List<UsageLogEntity>> = usageLogDao.getRecentLogs()
    val reminderCount: Flow<Int> = usageLogDao.getReminderCountFlow()
    val suppressedCount: Flow<Int> = usageLogDao.getSuppressedCountFlow()
    val sleepSchedules: Flow<List<SleepScheduleEntity>> = sleepScheduleDao.getAllSchedules()

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<ReminderSettings> = _settings.asStateFlow()

    fun loadSettings(): ReminderSettings {
        return ReminderSettings(
            intervalMinutes = prefs.getInt("interval_minutes", 10),
            soundEnabled = prefs.getBoolean("sound_enabled", true),
            vibrationEnabled = prefs.getBoolean("vibration_enabled", true),
            overlayEnabled = prefs.getBoolean("overlay_enabled", true)
        )
    }

    fun updateInterval(minutes: Int) {
        prefs.edit().putInt("interval_minutes", minutes).apply()
        _settings.value = _settings.value.copy(intervalMinutes = minutes)
    }

    fun updateSound(enabled: Boolean) {
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
        _settings.value = _settings.value.copy(soundEnabled = enabled)
    }

    fun updateVibration(enabled: Boolean) {
        prefs.edit().putBoolean("vibration_enabled", enabled).apply()
        _settings.value = _settings.value.copy(vibrationEnabled = enabled)
    }

    fun updateOverlay(enabled: Boolean) {
        prefs.edit().putBoolean("overlay_enabled", enabled).apply()
        _settings.value = _settings.value.copy(overlayEnabled = enabled)
    }

    suspend fun setAppIgnored(packageName: String, appName: String, isIgnored: Boolean) {
        if (isIgnored) {
            ignoredAppDao.setIgnored(IgnoredAppEntity(packageName, appName, isIgnored = true))
        } else {
            ignoredAppDao.remove(packageName)
        }
    }

    suspend fun isAppIgnored(packageName: String): Boolean {
        return ignoredAppDao.isAppIgnored(packageName)
    }

    suspend fun logAction(minutes: Int, packageName: String, appName: String, actionType: String) {
        usageLogDao.insert(
            UsageLogEntity(
                usageDurationMinutes = minutes,
                activePackage = packageName,
                activeAppName = appName,
                actionType = actionType
            )
        )
    }

    suspend fun clearHistory() {
        usageLogDao.clearLogs()
    }

    suspend fun saveSchedule(schedule: SleepScheduleEntity): Long {
        return sleepScheduleDao.insert(schedule)
    }

    suspend fun updateSchedule(schedule: SleepScheduleEntity) {
        sleepScheduleDao.update(schedule)
    }

    suspend fun deleteSchedule(id: Long) {
        sleepScheduleDao.deleteById(id)
    }

    suspend fun setScheduleEnabled(id: Long, isEnabled: Boolean) {
        sleepScheduleDao.setEnabled(id, isEnabled)
    }

    suspend fun getEnabledSchedules(): List<SleepScheduleEntity> {
        return sleepScheduleDao.getEnabledSchedulesList()
    }

    suspend fun ensureDefaultScheduleIfEmpty() {
        val existing = sleepScheduleDao.getAllSchedulesList()
        if (existing.isEmpty()) {
            // Pre-seed a default Sleep Schedule (11:00 PM to 7:00 AM)
            sleepScheduleDao.insert(
                SleepScheduleEntity(
                    title = "Night Sleep",
                    startHour = 23,
                    startMinute = 0,
                    endHour = 7,
                    endMinute = 0,
                    daysOfWeek = "MON,TUE,WED,THU,FRI,SAT,SUN",
                    isEnabled = true,
                    blockedPackages = ""
                )
            )
        }
    }

    suspend fun loadInstalledLauncherApps(): List<AppItem> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                mainIntent,
                PackageManager.ResolveInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(mainIntent, 0)
        }

        val currentIgnored = ignoredAppDao.getAll().first().associateBy { it.packageName }
        val ownPackage = context.packageName

        val list = mutableListOf<AppItem>()
        val seenPackages = mutableSetOf<String>()

        for (resolveInfo in resolveInfos) {
            val pkg = resolveInfo.activityInfo.packageName
            if (pkg == ownPackage || seenPackages.contains(pkg)) continue
            seenPackages.add(pkg)

            val label = try {
                resolveInfo.loadLabel(packageManager).toString()
            } catch (e: Exception) {
                pkg
            }

            val icon = try {
                resolveInfo.loadIcon(packageManager)
            } catch (e: Exception) {
                null
            }

            val isSystem = (resolveInfo.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isIgnored = currentIgnored[pkg]?.isIgnored ?: false

            list.add(
                AppItem(
                    packageName = pkg,
                    appName = label,
                    isIgnored = isIgnored,
                    isSystemApp = isSystem,
                    icon = icon
                )
            )
        }

        list.sortedWith(compareByDescending<AppItem> { it.isIgnored }.thenBy { it.appName.lowercase() })
    }
}
