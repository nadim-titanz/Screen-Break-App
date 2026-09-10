package com.example.model

data class UsageState(
    val continuousSeconds: Long = 0L,
    val intervalMinutes: Int = 10,
    val isServiceRunning: Boolean = false,
    val isScreenOn: Boolean = true,
    val activePackage: String = "",
    val activeAppName: String = "",
    val isCurrentAppIgnored: Boolean = false,
    val lastReminderTime: Long = 0L,
    val lockedPackage: String = "",
    val lockedAppName: String = "",
    val lockedSecondsRemaining: Int = 0,
    val isSleepScheduleActive: Boolean = false,
    val activeSleepScheduleTitle: String = "",
    val emergencySnoozeSecondsRemaining: Int = 0,
    val emergencySnoozedPackage: String = ""
) {
    val progress: Float
        get() {
            val targetSeconds = (intervalMinutes * 60).coerceAtLeast(1)
            val currentCycleSeconds = continuousSeconds % targetSeconds
            return (currentCycleSeconds.toFloat() / targetSeconds.toFloat()).coerceIn(0f, 1f)
        }

    val formattedTime: String
        get() {
            val minutes = continuousSeconds / 60
            val seconds = continuousSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }

    val formattedTarget: String
        get() = "%02d:00".format(intervalMinutes)
}
