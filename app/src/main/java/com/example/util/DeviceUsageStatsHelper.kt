package com.example.util

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.graphics.drawable.Drawable
import com.example.data.UsageLogEntity
import com.example.service.CurrentAppDetector
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DayActivity(
    val dayName: String,     // "SUN", "MON", "TUE", etc.
    val dayNumber: Int,      // 21, 22, 23, etc.
    val dateLabel: String,   // "SUN, JUN 21" or "TODAY"
    val durationMinutes: Int,// Real measured minutes of screen time
    val sessionCount: Int,   // Number of app / unlock sessions
    val isToday: Boolean,
    val isFuture: Boolean
)

data class AppUsageItem(
    val packageName: String,
    val appName: String,
    val usageMinutes: Int,
    val percentageOfTotal: Float,
    val icon: Drawable? = null
)

object DeviceUsageStatsHelper {

    /**
     * Calculates exact foreground usage durations per package using Android's UsageEvents API.
     * Correctly handles session start (ACTIVITY_RESUMED), session termination (ACTIVITY_PAUSED, ACTIVITY_STOPPED),
     * and screen lock/off intervals (SCREEN_NON_INTERACTIVE, KEYGUARD_SHOWN).
     */
    fun calculateAppUsageFromEvents(
        context: Context,
        startTime: Long,
        endTime: Long,
        isCurrentDay: Boolean = false
    ): Map<String, Long> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyMap()

        val usageMap = mutableMapOf<String, Long>()
        var eventsFound = false

        try {
            val events = usageStatsManager.queryEvents(startTime, endTime)
            val event = UsageEvents.Event()
            var currentFgPkg: String? = null
            var currentFgStartTime = 0L

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                eventsFound = true
                val pkg = event.packageName ?: continue
                val timestamp = event.timeStamp

                when (event.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED -> {
                        if (currentFgPkg != null) {
                            val duration = (timestamp - currentFgStartTime).coerceAtLeast(0L)
                            if (duration > 0 && duration < 86_400_000L) {
                                usageMap[currentFgPkg] = (usageMap[currentFgPkg] ?: 0L) + duration
                            }
                        }
                        currentFgPkg = pkg
                        currentFgStartTime = timestamp
                    }

                    UsageEvents.Event.ACTIVITY_PAUSED,
                    UsageEvents.Event.ACTIVITY_STOPPED -> {
                        if (currentFgPkg == pkg) {
                            val duration = (timestamp - currentFgStartTime).coerceAtLeast(0L)
                            if (duration > 0 && duration < 86_400_000L) {
                                usageMap[pkg] = (usageMap[pkg] ?: 0L) + duration
                            }
                            currentFgPkg = null
                            currentFgStartTime = 0L
                        } else if (currentFgPkg == null) {
                            // App was active before the interval start boundary
                            val duration = (timestamp - startTime).coerceAtLeast(0L)
                            if (duration > 0 && duration < 86_400_000L) {
                                usageMap[pkg] = (usageMap[pkg] ?: 0L) + duration
                            }
                        }
                    }

                    UsageEvents.Event.SCREEN_NON_INTERACTIVE,
                    UsageEvents.Event.KEYGUARD_SHOWN -> {
                        if (currentFgPkg != null) {
                            val duration = (timestamp - currentFgStartTime).coerceAtLeast(0L)
                            if (duration > 0 && duration < 86_400_000L) {
                                usageMap[currentFgPkg] = (usageMap[currentFgPkg] ?: 0L) + duration
                            }
                            currentFgPkg = null
                            currentFgStartTime = 0L
                        }
                    }
                }
            }

            // If an app is still in foreground right now at endTime
            if (isCurrentDay && currentFgPkg != null && currentFgStartTime > 0) {
                val duration = (endTime - currentFgStartTime).coerceAtLeast(0L)
                if (duration > 0 && duration < 86_400_000L) {
                    usageMap[currentFgPkg] = (usageMap[currentFgPkg] ?: 0L) + duration
                }
            }
        } catch (e: Exception) {
            // queryEvents failed or permission denied
        }

        // Fallback to queryUsageStats if queryEvents had no entries (e.g. system buffer rotated)
        if (!eventsFound || usageMap.isEmpty()) {
            try {
                val statsList = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    startTime,
                    endTime
                )
                if (!statsList.isNullOrEmpty()) {
                    for (stat in statsList) {
                        if (stat.totalTimeInForeground > 0) {
                            val existing = usageMap[stat.packageName] ?: 0L
                            usageMap[stat.packageName] = maxOf(existing, stat.totalTimeInForeground)
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }

        return usageMap
    }

    /**
     * Accurately queries real system usage stats from Android's UsageEvents
     * for each day of the current week (Sunday to Saturday).
     */
    fun getWeeklyUsageStats(
        context: Context,
        logs: List<UsageLogEntity>,
        liveContinuousSeconds: Long = 0L
    ): List<DayActivity> {
        val calendar = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.SUNDAY
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val nowCal = Calendar.getInstance()
        val nowDayOfYear = nowCal.get(Calendar.DAY_OF_YEAR)
        val nowYear = nowCal.get(Calendar.YEAR)
        val currentTimeMs = System.currentTimeMillis()

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
        val hasUsageAccess = CurrentAppDetector.hasUsageAccess(context)

        val dayNames = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
        val days = mutableListOf<DayActivity>()

        val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

        for (i in 0..6) {
            val dayStart = calendar.timeInMillis
            val dayEnd = dayStart + 86400000L - 1L

            val dayNum = calendar.get(Calendar.DAY_OF_MONTH)
            val isToday = calendar.get(Calendar.DAY_OF_YEAR) == nowDayOfYear && calendar.get(Calendar.YEAR) == nowYear
            val isFuture = calendar.after(nowCal) && !isToday

            var measuredMinutes = 0
            var sessionCount = 0

            if (!isFuture) {
                if (hasUsageAccess && usageStatsManager != null) {
                    try {
                        val queryEnd = minOf(dayEnd, currentTimeMs)
                        val usageDurations = calculateAppUsageFromEvents(
                            context = context,
                            startTime = dayStart,
                            endTime = queryEnd,
                            isCurrentDay = isToday
                        )

                        var totalForegroundMs = 0L
                        for ((pkg, duration) in usageDurations) {
                            if (pkg.isNotEmpty() &&
                                duration > 0 &&
                                !CurrentAppDetector.isSystemUI(pkg) &&
                                !CurrentAppDetector.isLauncherApp(context, pkg) &&
                                pkg != context.packageName
                            ) {
                                totalForegroundMs += duration
                            }
                        }
                        measuredMinutes = (totalForegroundMs / (1000L * 60L)).toInt()

                        // Query real sessions via events
                        try {
                            val events = usageStatsManager.queryEvents(dayStart, queryEnd)
                            val event = UsageEvents.Event()
                            var resumes = 0
                            while (events.hasNextEvent()) {
                                events.getNextEvent(event)
                                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED &&
                                    !CurrentAppDetector.isSystemUI(event.packageName ?: "") &&
                                    !CurrentAppDetector.isLauncherApp(context, event.packageName ?: "") &&
                                    event.packageName != context.packageName
                                ) {
                                    resumes++
                                }
                            }
                            sessionCount = resumes
                        } catch (e: Exception) {
                            if (measuredMinutes > 0) {
                                sessionCount = (measuredMinutes / 15).coerceAtLeast(1)
                            }
                        }
                    } catch (e: Exception) {
                        // Fallback below
                    }
                }

                // If today, incorporate live continuous screen time if higher
                if (isToday && liveContinuousSeconds > 0) {
                    val liveMinutes = (liveContinuousSeconds / 60L).toInt()
                    if (liveMinutes > measuredMinutes) {
                        measuredMinutes = liveMinutes
                    }
                    if (sessionCount == 0 && measuredMinutes > 0) {
                        sessionCount = 1
                    }
                }

                // Complement with recorded database action logs for this day
                val logsForDay = logs.filter { it.timestamp in dayStart..dayEnd }
                if (logsForDay.isNotEmpty()) {
                    val logMinutes = logsForDay.sumOf { it.usageDurationMinutes }
                    if (logMinutes > measuredMinutes) {
                        measuredMinutes = logMinutes
                    }
                    if (logsForDay.size > sessionCount) {
                        sessionCount = logsForDay.size
                    }
                }
            }

            val dateStr = if (isToday) "TODAY" else dateFormat.format(Date(dayStart)).uppercase()

            days.add(
                DayActivity(
                    dayName = dayNames[i],
                    dayNumber = dayNum,
                    dateLabel = dateStr,
                    durationMinutes = measuredMinutes,
                    sessionCount = sessionCount,
                    isToday = isToday,
                    isFuture = isFuture
                )
            )

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return days
    }

    /**
     * Returns detailed per-app usage stats for today, sorted by duration descending.
     */
    fun getTodayAppUsageList(context: Context): List<AppUsageItem> {
        if (!CurrentAppDetector.hasUsageAccess(context)) return emptyList()

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = cal.timeInMillis
        val endTime = System.currentTimeMillis()

        return try {
            val usageDurations = calculateAppUsageFromEvents(
                context = context,
                startTime = startTime,
                endTime = endTime,
                isCurrentDay = true
            )
            val pm = context.packageManager
            val rawList = mutableListOf<Triple<String, String, Long>>()

            for ((pkg, duration) in usageDurations) {
                if (duration >= 5_000L && // Include any app used for >= 5 seconds
                    !CurrentAppDetector.isSystemUI(pkg) &&
                    !CurrentAppDetector.isLauncherApp(context, pkg) &&
                    pkg != context.packageName
                ) {
                    val appName = try {
                        val appInfo = pm.getApplicationInfo(pkg, 0)
                        pm.getApplicationLabel(appInfo).toString()
                    } catch (e: Exception) {
                        CurrentAppDetector.getAppName(context, pkg)
                    }
                    rawList.add(Triple(pkg, appName.ifEmpty { pkg }, duration))
                }
            }

            val totalTimeMs = rawList.sumOf { it.third }.coerceAtLeast(1L)
            rawList.sortedByDescending { it.third }.map { (pkg, name, ms) ->
                val icon = try {
                    pm.getApplicationIcon(pkg)
                } catch (e: Exception) {
                    null
                }
                val minutes = (ms / (1000L * 60L)).toInt().coerceAtLeast(1)
                val percentage = (ms.toFloat() / totalTimeMs.toFloat())
                AppUsageItem(
                    packageName = pkg,
                    appName = name,
                    usageMinutes = minutes,
                    percentageOfTotal = percentage,
                    icon = icon
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getTodayTotalMinutes(context: Context): Int {
        if (!CurrentAppDetector.hasUsageAccess(context)) return 0

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = cal.timeInMillis
        val endTime = System.currentTimeMillis()

        val usageDurations = calculateAppUsageFromEvents(
            context = context,
            startTime = startTime,
            endTime = endTime,
            isCurrentDay = true
        )
        var totalMs = 0L
        for ((pkg, duration) in usageDurations) {
            if (duration > 0 &&
                !CurrentAppDetector.isSystemUI(pkg) &&
                !CurrentAppDetector.isLauncherApp(context, pkg) &&
                pkg != context.packageName
            ) {
                totalMs += duration
            }
        }
        return (totalMs / (1000L * 60L)).toInt().coerceAtLeast(if (totalMs > 30_000L) 1 else 0)
    }

    fun getYesterdayTotalMinutes(context: Context): Int {
        if (!CurrentAppDetector.hasUsageAccess(context)) return 0

        val calToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endTime = calToday.timeInMillis - 1L
        val startTime = calToday.timeInMillis - (24 * 60 * 60 * 1000L)

        val usageDurations = calculateAppUsageFromEvents(
            context = context,
            startTime = startTime,
            endTime = endTime,
            isCurrentDay = false
        )
        var totalMs = 0L
        for ((pkg, duration) in usageDurations) {
            if (duration > 0 &&
                !CurrentAppDetector.isSystemUI(pkg) &&
                !CurrentAppDetector.isLauncherApp(context, pkg) &&
                pkg != context.packageName
            ) {
                totalMs += duration
            }
        }
        return (totalMs / (1000L * 60L)).toInt()
    }

    fun getScreenTimeChangePercentage(context: Context): Double {
        val today = getTodayTotalMinutes(context)
        val yesterday = getYesterdayTotalMinutes(context)
        if (yesterday <= 0) return 0.0
        return ((today - yesterday).toDouble() / yesterday.toDouble()) * 100.0
    }

    fun calculateAverageMinutes(days: List<DayActivity>): Int {
        val activeDays = days.filter { !it.isFuture && it.durationMinutes > 0 }
        if (activeDays.isEmpty()) return 0
        return activeDays.sumOf { it.durationMinutes } / activeDays.size
    }

    fun formatDuration(minutes: Int): String {
        if (minutes <= 0) return "0m"
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        return if (hours > 0) {
            "${hours}h ${remainingMinutes.toString().padStart(2, '0')}m"
        } else {
            "${remainingMinutes}m"
        }
    }
}
