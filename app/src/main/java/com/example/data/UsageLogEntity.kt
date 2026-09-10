package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usage_logs")
data class UsageLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val usageDurationMinutes: Int,
    val activePackage: String,
    val activeAppName: String,
    val actionType: String // "REMINDER_SHOWN", "REMINDER_SUPPRESSED", "SNOOZED", "DISMISSED"
)
