package com.example.model

data class ReminderSettings(
    val intervalMinutes: Int = 10,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val overlayEnabled: Boolean = true
)
