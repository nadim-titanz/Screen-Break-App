package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import com.example.util.ReelsTrackingManager

class ReelsAndUsageAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "ReelsBlockerService"

        val TIKTOK_PACKAGES = hashSetOf(
            "com.ss.android.ugc.trill",
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.aweme",
            "com.zhiliaoapp.musically.go"
        )

        val YOUTUBE_PACKAGES = hashSetOf(
            "com.google.android.youtube",
            "app.revanced.android.youtube",
            "com.google.android.youtube.tv"
        )

        const val INSTAGRAM_PACKAGE = "com.instagram.android"
        const val FACEBOOK_PACKAGE = "com.facebook.katana"
        const val FACEBOOK_LITE_PACKAGE = "com.facebook.lite"
        const val SNAPCHAT_PACKAGE = "com.snapchat.android"

        var isServiceConnected: Boolean = false
            private set

        fun isAccessibilityEnabled(context: Context): Boolean {
            val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? android.view.accessibility.AccessibilityManager
                ?: return false
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            return enabledServices.contains(context.packageName)
        }
    }

    private var lastBlockedAlertTime = 0L
    private var lastTrackedPackage = ""
    private var lastReelIndex = -1
    private var lastScrollTime = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceConnected = true
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_VIEW_SCROLLED or
                AccessibilityEvent.TYPE_VIEW_SELECTED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.DEFAULT or
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 80
        }
        serviceInfo = info
        Log.d(TAG, "Reels and Usage Accessibility Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val packageName = event.packageName?.toString() ?: return

        // 1. Check for Reels / Shorts Blocking
        val isBlockingEnabled = ReelsTrackingManager.isReelsBlockingEnabled(this)
        if (isBlockingEnabled) {
            val rootNode = rootInActiveWindow
            if (rootNode != null) {
                val isReelActive = isReelOpen(rootNode, packageName, event)
                if (isReelActive) {
                    val now = SystemClock.uptimeMillis()
                    if (now - lastBlockedAlertTime > 2000L) {
                        lastBlockedAlertTime = now
                        Toast.makeText(
                            applicationContext,
                            "⚡ REELS BLOCKED // DETOX ACTIVE",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    // Block by navigating back, or home if TikTok
                    if (TIKTOK_PACKAGES.contains(packageName)) {
                        performGlobalAction(GLOBAL_ACTION_HOME)
                    } else {
                        performGlobalAction(GLOBAL_ACTION_BACK)
                    }
                    return
                }
            }
        }

        // 2. Track Reel Scrolls
        if (ReelsTrackingManager.isReelsTrackingEnabled(this)) {
            val isScrollOrSelect = event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED ||
                event.eventType == AccessibilityEvent.TYPE_VIEW_SELECTED

            if (isScrollOrSelect) {
                handleScrollEvent(event, packageName)
            }
        }
    }

    private fun isReelOpen(rootNode: AccessibilityNodeInfo, packageName: String, event: AccessibilityEvent?): Boolean {
        // TikTok is 100% full-screen short-form videos
        if (TIKTOK_PACKAGES.contains(packageName)) {
            return true
        }

        // Instagram Reels check
        if (packageName == INSTAGRAM_PACKAGE) {
            if (hasMatchingNode(rootNode, listOf("clips", "root_clips_layout", "clips_viewer", "reel_viewer", "clips_timeline", "clips_video_container"))) {
                return true
            }
            val cls = event?.className?.toString() ?: ""
            if (cls.contains("Clips", ignoreCase = true) || cls.contains("ReboundViewPager", ignoreCase = true)) {
                return true
            }
        }

        // YouTube Shorts check
        if (YOUTUBE_PACKAGES.contains(packageName)) {
            if (hasMatchingNode(rootNode, listOf("reel_recycler", "shorts_container", "reel_watch_fragment", "reel_player", "shorts_player", "reel_watch_pager"))) {
                return true
            }
            val cls = event?.className?.toString() ?: ""
            if (cls.contains("Reel", ignoreCase = true) || cls.contains("Shorts", ignoreCase = true)) {
                return true
            }
        }

        // Facebook Reels check
        if (packageName == FACEBOOK_PACKAGE || packageName == FACEBOOK_LITE_PACKAGE) {
            if (hasMatchingNode(rootNode, listOf("fb_shorts", "FbShorts", "reels_viewer", "short_video"))) {
                return true
            }
        }

        return false
    }

    private fun handleScrollEvent(event: AccessibilityEvent, packageName: String) {
        val root = rootInActiveWindow ?: return
        val inReels = isReelOpen(root, packageName, event)
        if (!inReels) return

        val now = SystemClock.uptimeMillis()

        // If switched to a new app, reset index tracking
        if (packageName != lastTrackedPackage) {
            lastTrackedPackage = packageName
            lastReelIndex = -1
            lastScrollTime = 0L
        }

        val fromIndex = event.fromIndex
        var shouldCount = false

        // Approach 1: Adapter index tracking (RecyclerView / ViewPager2)
        if (fromIndex >= 0) {
            if (lastReelIndex == -1) {
                // First reel view observed in this session
                lastReelIndex = fromIndex
                lastScrollTime = now
            } else if (fromIndex != lastReelIndex && (now - lastScrollTime > 500L)) {
                // Verified distinct item scrolled
                lastReelIndex = fromIndex
                lastScrollTime = now
                shouldCount = true
            }
        } else {
            // Approach 2: Time-debounced scroll event for custom viewpagers
            if (now - lastScrollTime > 850L) {
                // Validate vertical scroll motion
                var hasMotion = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    hasMotion = event.scrollDeltaY != 0
                }
                if (!hasMotion) {
                    hasMotion = event.itemCount > 0 || event.scrollY != 0 || event.source != null
                }

                if (hasMotion) {
                    lastScrollTime = now
                    shouldCount = true
                }
            }
        }

        if (shouldCount) {
            val totalToday = ReelsTrackingManager.incrementReelsCount(this, 1)
            Log.d(TAG, "Reel scrolled! Today total: $totalToday in $packageName")
        }
    }

    private fun hasMatchingNode(rootNode: AccessibilityNodeInfo, targetKeywords: List<String>): Boolean {
        return try {
            for (keyword in targetKeywords) {
                val list = rootNode.findAccessibilityNodeInfosByViewId(keyword)
                if (list.isNotEmpty()) return true

                // Also check package-prefixed view ID
                val pkgName = rootNode.packageName?.toString() ?: ""
                if (pkgName.isNotEmpty()) {
                    val fullId = "$pkgName:id/$keyword"
                    val fullList = rootNode.findAccessibilityNodeInfosByViewId(fullId)
                    if (fullList.isNotEmpty()) return true
                }
            }

            // Quick check in active node text / contentDescription
            for (keyword in targetKeywords) {
                val textNodes = rootNode.findAccessibilityNodeInfosByText(keyword)
                if (textNodes.isNotEmpty()) return true
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceConnected = false
    }
}
