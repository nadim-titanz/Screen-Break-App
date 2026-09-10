package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ignored_apps")
data class IgnoredAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isIgnored: Boolean = true,
    val addedAt: Long = System.currentTimeMillis()
)
