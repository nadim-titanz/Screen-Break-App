package com.example.util

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
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

object DeviceUsageStatsHelper {

    /**
     * Accurately queries real system usage stats from Android's UsageStatsManager
     * for each day of the current week (Sunday to Saturday), with real foreground
     * screen times, session counts, and live service synchronization.
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
            val dayEnd = dayStart + 86400000L - 1L // 23:59:59.999

            val dayNum = calendar.get(Calendar.DAY_OF_MONTH)
            val isToday = calendar.get(Calendar.DAY_OF_YEAR) == nowDayOfYear && calendar.get(Calendar.YEAR) == nowYear
            val isFuture = calendar.after(nowCal) && !isToday

            var measuredMinutes = 0
            var sessionCount = 0

            if (!isFuture) {
                // Query real foreground time from UsageStatsManager
                if (hasUsageAccess && usageStatsManager != null) {
                    try {
                        val queryEnd = minOf(dayEnd, currentTimeMs)
                        val aggregated = usageStatsManager.queryAndAggregateUsageStats(dayStart, queryEnd)
                        var totalForegroundMs = 0L

                        for ((pkg, stat) in aggregated) {
                            if (pkg.isNotEmpty() &&
                                stat.totalTimeInForeground > 0 &&
                                !CurrentAppDetector.isSystemUI(pkg) &&
                                !CurrentAppDetector.isLauncherApp(context, pkg)
                            ) {
                                totalForegroundMs += stat.totalTimeInForeground
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
                                    !CurrentAppDetector.isSystemUI(event.packageName) &&
                                    !CurrentAppDetector.isLauncherApp(context, event.packageName)
                                ) {
                                    resumes++
                                }
                            }
                            sessionCount = resumes
                        } catch (e: Exception) {
                            // Fallback estimation
                            if (measuredMinutes > 0) {
                                sessionCount = (measuredMinutes / 18).coerceAtLeast(1)
                            }
                        }
                    } catch (e: Exception) {
                        // Fallback below
                    }
                }

                // If today, incorporate live continuous screen time if higher than aggregated flush
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
