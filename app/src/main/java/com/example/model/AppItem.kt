package com.example.model

import android.graphics.drawable.Drawable

data class AppItem(
    val packageName: String,
    val appName: String,
    val isIgnored: Boolean,
    val isSystemApp: Boolean = false,
    val icon: Drawable? = null
)
