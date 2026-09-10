package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "sleep_schedules")
data class SleepScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String = "Night Sleep",
    val startHour: Int = 23,
    val startMinute: Int = 0,
    val endHour: Int = 7,
    val endMinute: Int = 0,
    val daysOfWeek: String = "MON,TUE,WED,THU,FRI,SAT,SUN",
    val isEnabled: Boolean = true,
    val blockedPackages: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getBlockedPackageSet(): Set<String> {
        if (blockedPackages.isBlank()) return emptySet()
        return blockedPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    fun getDaysSet(): Set<String> {
        if (daysOfWeek.isBlank()) return emptySet()
        return daysOfWeek.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    fun formattedTimeRange(): String {
        val startFormatted = formatTime12h(startHour, startMinute)
        val endFormatted = formatTime12h(endHour, endMinute)
        return "$startFormatted – $endFormatted"
    }

    fun isAppBlocked(packageName: String): Boolean {
        return getBlockedPackageSet().contains(packageName)
    }

    fun isActiveAt(calendar: Calendar = Calendar.getInstance()): Boolean {
        if (!isEnabled) return false

        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val todayCode = when (dayOfWeek) {
            Calendar.MONDAY -> "MON"
            Calendar.TUESDAY -> "TUE"
            Calendar.WEDNESDAY -> "WED"
            Calendar.THURSDAY -> "THU"
            Calendar.FRIDAY -> "FRI"
            Calendar.SATURDAY -> "SAT"
            Calendar.SUNDAY -> "SUN"
            else -> "MON"
        }

        val yesterdayCode = when (dayOfWeek) {
            Calendar.MONDAY -> "SUN"
            Calendar.TUESDAY -> "MON"
            Calendar.WEDNESDAY -> "TUE"
            Calendar.THURSDAY -> "WED"
            Calendar.FRIDAY -> "THU"
            Calendar.SATURDAY -> "FRI"
            Calendar.SUNDAY -> "SAT"
            else -> "SUN"
        }

        val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val startMinutes = startHour * 60 + startMinute
        val endMinutes = endHour * 60 + endMinute

        val days = getDaysSet()

        return if (startMinutes == endMinutes) {
            false
        } else if (startMinutes < endMinutes) {
            // Same-day schedule (e.g. 13:00 to 15:00)
            days.contains(todayCode) && currentMinutes >= startMinutes && currentMinutes < endMinutes
        } else {
            // Cross-midnight schedule (e.g. 23:00 to 07:00)
            if (currentMinutes >= startMinutes) {
                // Late night portion: belongs to today's schedule
                days.contains(todayCode)
            } else if (currentMinutes < endMinutes) {
                // Early morning portion: belongs to yesterday's night schedule
                days.contains(yesterdayCode)
            } else {
                false
            }
        }
    }

    companion object {
        val ALL_DAYS = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
        val WEEKDAYS = listOf("MON", "TUE", "WED", "THU", "FRI")
        val WEEKENDS = listOf("SAT", "SUN")

        fun formatTime12h(hour: Int, minute: Int): String {
            val period = if (hour >= 12) "PM" else "AM"
            val displayHour = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            return "%d:%02d %s".format(displayHour, minute, period)
        }
    }
}
