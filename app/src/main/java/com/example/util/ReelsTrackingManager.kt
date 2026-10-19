package com.example.util

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.example.ui.widgets.ReelsWidgetProvider
import com.example.ui.widgets.ScreentimeWidgetProvider
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object ReelsTrackingManager {

    private const val PREFS_NAME = "screen_break_reels_prefs"
    private const val KEY_REELS_DATA = "reels_scrolled_data"
    private const val KEY_BLOCK_REELS = "block_reels_enabled"
    private const val KEY_CENTER_TIMER = "center_timer_overlay_enabled"
    private const val KEY_TRACK_REELS = "track_reels_counter_enabled"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getTodayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    fun getYesterdayDate(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    fun getReelsMap(context: Context): Map<String, Int> {
        val jsonStr = getPrefs(context).getString(KEY_REELS_DATA, null) ?: return emptyMap()
        val result = mutableMapOf<String, Int>()
        try {
            val json = JSONObject(jsonStr)
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                result[key] = json.optInt(key, 0)
            }
        } catch (e: Exception) {
            // ignore
        }
        return result
    }

    private val _todayReelsFlow = kotlinx.coroutines.flow.MutableStateFlow(0)
    val todayReelsFlow: kotlinx.coroutines.flow.StateFlow<Int> = _todayReelsFlow

    fun getTodayCount(context: Context): Int {
        val count = getReelsMap(context)[getTodayDate()] ?: 0
        if (_todayReelsFlow.value != count) {
            _todayReelsFlow.value = count
        }
        return count
    }

    fun getYesterdayCount(context: Context): Int {
        return getReelsMap(context)[getYesterdayDate()] ?: 0
    }

    fun getChangePercentage(context: Context): Double {
        val today = getTodayCount(context)
        val yesterday = getYesterdayCount(context)
        if (yesterday <= 0) return 0.0
        return ((today - yesterday).toDouble() / yesterday.toDouble()) * 100.0
    }

    @Synchronized
    fun incrementReelsCount(context: Context, delta: Int = 1): Int {
        val currentMap = getReelsMap(context).toMutableMap()
        val today = getTodayDate()
        val newCount = (currentMap[today] ?: 0) + delta
        currentMap[today] = newCount

        val json = JSONObject()
        for ((k, v) in currentMap) {
            json.put(k, v)
        }
        getPrefs(context).edit().putString(KEY_REELS_DATA, json.toString()).apply()

        _todayReelsFlow.value = newCount

        // Notify homescreen widgets to refresh immediately
        updateWidgets(context)

        return newCount
    }

    fun isReelsBlockingEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_BLOCK_REELS, false)
    }

    fun setReelsBlockingEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_BLOCK_REELS, enabled).apply()
        updateWidgets(context)
    }

    fun isCenterTimerOverlayEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_CENTER_TIMER, true)
    }

    fun setCenterTimerOverlayEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_CENTER_TIMER, enabled).apply()
    }

    fun isReelsTrackingEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_TRACK_REELS, true)
    }

    fun setReelsTrackingEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_TRACK_REELS, enabled).apply()
    }

    /**
     * Returns list of counts for the last 7 days (day 6 ago to today)
     */
    fun getLast7DaysReels(context: Context): List<Pair<String, Int>> {
        val map = getReelsMap(context)
        val result = mutableListOf<Pair<String, Int>>()
        val cal = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("EEE", Locale.US)
        val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        for (i in 6 downTo 0) {
            val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val key = keyFormat.format(c.time)
            val label = if (i == 0) "TODAY" else dayFormat.format(c.time).uppercase()
            result.add(label to (map[key] ?: 0))
        }
        return result
    }

    fun updateWidgets(context: Context) {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            
            // Reels Widget
            val reelsComponent = ComponentName(context, ReelsWidgetProvider::class.java)
            val reelsIds = appWidgetManager.getAppWidgetIds(reelsComponent)
            if (reelsIds.isNotEmpty()) {
                val intent = Intent(context, ReelsWidgetProvider::class.java).apply {
                    action = ReelsWidgetProvider.ACTION_WIDGET_REFRESH
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, reelsIds)
                }
                context.sendBroadcast(intent)
            }

            // Screen time widget
            val screenComponent = ComponentName(context, ScreentimeWidgetProvider::class.java)
            val screenIds = appWidgetManager.getAppWidgetIds(screenComponent)
            if (screenIds.isNotEmpty()) {
                val intent = Intent(context, ScreentimeWidgetProvider::class.java).apply {
                    action = ScreentimeWidgetProvider.ACTION_WIDGET_REFRESH
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, screenIds)
                }
                context.sendBroadcast(intent)
            }
        } catch (e: Exception) {
            // ignore
        }
    }
}
