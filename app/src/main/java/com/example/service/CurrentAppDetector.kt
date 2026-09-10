package com.example.service

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.util.LruCache

object CurrentAppDetector {

    @Volatile
    private var lastKnownForegroundPackage: String = ""

    private val appNameCache = object : LruCache<String, String>(100) {}
    private val launcherCache = object : LruCache<String, Boolean>(50) {}

    fun hasUsageAccess(context: Context): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }

    fun hasOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun getUsageAccessIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun getOverlayPermissionIntent(context: Context): Intent {
        return Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    /**
     * Determines the currently active foreground application.
     * Uses a multi-tiered approach:
     * 1. Query UsageEvents over the last 15 minutes (fast) or 2 hours fallback.
     * 2. Fallback to aggregated UsageStats (lastTimeUsed) over 24 hours.
     * 3. Fallback to cached lastKnownForegroundPackage.
     */
    fun getForegroundApp(context: Context): Pair<String, String> {
        if (!hasUsageAccess(context)) {
            return Pair("", "")
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return Pair("", "")

        val endTime = System.currentTimeMillis()
        // Fast query over recent 15 minutes to keep iteration lightweight
        val startTime = endTime - (15L * 60 * 1000L)

        var detectedPackage = ""
        var lastResumeTimestamp = 0L

        try {
            val events = usageStatsManager.queryEvents(startTime, endTime)
            val event = UsageEvents.Event()

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                    if (event.timeStamp >= lastResumeTimestamp) {
                        lastResumeTimestamp = event.timeStamp
                        detectedPackage = event.packageName
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback
        }

        // Fallback 1: Retain last known package if valid and recent query had no resume events
        if (detectedPackage.isEmpty() && lastKnownForegroundPackage.isNotEmpty()) {
            detectedPackage = lastKnownForegroundPackage
        }

        // Fallback 2: Query aggregated UsageStats if still empty
        if (detectedPackage.isEmpty()) {
            try {
                val stats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_BEST,
                    endTime - (60L * 60 * 1000L),
                    endTime
                )
                val latest = stats?.filter { it.packageName.isNotEmpty() }
                    ?.maxByOrNull { it.lastTimeUsed }
                if (latest != null && latest.packageName.isNotEmpty()) {
                    detectedPackage = latest.packageName
                }
            } catch (e: Exception) {
                // Ignore
            }
        }

        if (detectedPackage.isNotEmpty()) {
            lastKnownForegroundPackage = detectedPackage
        } else {
            return Pair("", "")
        }

        val appName = getAppName(context, detectedPackage)
        return Pair(detectedPackage, appName)
    }

    /**
     * Checks whether a package is a device home launcher.
     */
    fun isLauncherApp(context: Context, packageName: String): Boolean {
        if (packageName.isEmpty()) return false
        val cached = launcherCache.get(packageName)
        if (cached != null) return cached

        val isLauncher = try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
            val pm = context.packageManager
            val defaultLauncher = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.resolveActivity(intent, PackageManager.ResolveInfoFlags.of(0))?.activityInfo?.packageName
            } else {
                @Suppress("DEPRECATION")
                pm.resolveActivity(intent, 0)?.activityInfo?.packageName
            }
            if (packageName == defaultLauncher) {
                true
            } else {
                val launchers = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    pm.queryIntentActivities(intent, 0)
                }
                launchers.any { it.activityInfo.packageName == packageName }
            }
        } catch (e: Exception) {
            false
        }
        launcherCache.put(packageName, isLauncher)
        return isLauncher
    }

    /**
     * Checks whether a package is the phone dialer or emergency in-call service.
     */
    fun isDialerOrEmergency(context: Context, packageName: String): Boolean {
        if (packageName.isEmpty()) return false
        val lower = packageName.lowercase()
        if (lower == "com.android.phone" ||
            lower == "com.android.server.telecom" ||
            lower == "com.android.incallui" ||
            lower.contains("telecom") ||
            lower.contains("dialer") ||
            lower.contains("emergency")
        ) return true

        return try {
            val dialIntent = Intent(Intent.ACTION_DIAL)
            val dialerPkg = context.packageManager.resolveActivity(dialIntent, 0)?.activityInfo?.packageName
            packageName == dialerPkg
        } catch (e: Exception) {
            false
        }
    }

    /**
     * System UI and Android core framework packages.
     */
    fun isSystemUI(packageName: String): Boolean {
        val lower = packageName.lowercase()
        return lower == "com.android.systemui" || lower == "android"
    }

    /**
     * Packages that should NEVER be kicked out during break lockouts.
     */
    fun isExemptFromLockout(context: Context, packageName: String): Boolean {
        if (packageName.isEmpty()) return true
        if (packageName == context.packageName) return true
        if (isSystemUI(packageName)) return true
        if (isLauncherApp(context, packageName)) return true
        if (isDialerOrEmergency(context, packageName)) return true
        return false
    }

    fun getAppName(context: Context, packageName: String): String {
        if (packageName.isEmpty()) return ""
        val cached = appNameCache.get(packageName)
        if (cached != null) return cached

        val pm = context.packageManager
        val name = try {
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getApplicationInfo(packageName, 0)
            }
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
        appNameCache.put(packageName, name)
        return name
    }
}
