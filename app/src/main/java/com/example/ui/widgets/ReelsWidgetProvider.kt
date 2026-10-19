package com.example.ui.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.util.ReelsTrackingManager

class ReelsWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_WIDGET_REFRESH = "com.example.reels.WIDGET_REFRESH"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_WIDGET_REFRESH ||
            intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE
        ) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                ?: appWidgetManager.getAppWidgetIds(
                    android.content.ComponentName(context, ReelsWidgetProvider::class.java)
                )
            widgetIds.forEach { widgetId ->
                updateWidget(context, appWidgetManager, widgetId)
            }
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int
    ) {
        try {
            val todayCount = ReelsTrackingManager.getTodayCount(context)
            val changePercentage = ReelsTrackingManager.getChangePercentage(context)
            val isBlocking = ReelsTrackingManager.isReelsBlockingEnabled(context)

            val views = RemoteViews(context.packageName, R.layout.widget_reels_count).apply {
                // Formatted count
                setTextViewText(R.id.widget_reels_cout, formatCount(todayCount.toLong()))

                // Percentage indicator
                val percentText = when {
                    changePercentage > 0 -> "+%.1f%%".format(changePercentage)
                    changePercentage < 0 -> "%.1f%%".format(changePercentage)
                    else -> "0.0%"
                }
                setTextViewText(R.id.widget_reels_cout_percentage, percentText)
                val color = when {
                    changePercentage > 0 -> Color.parseColor("#D71921") // red increase
                    changePercentage < 0 -> Color.parseColor("#4CAF50") // green decrease
                    else -> Color.parseColor("#888888")
                }
                setTextColor(R.id.widget_reels_cout_percentage, color)

                // Blocking status indicator
                if (isBlocking) {
                    setTextViewText(R.id.widget_reels_block_status, "● BLOCKED")
                    setTextColor(R.id.widget_reels_block_status, Color.parseColor("#D71921"))
                } else {
                    setTextViewText(R.id.widget_reels_block_status, "● TRACKING")
                    setTextColor(R.id.widget_reels_block_status, Color.parseColor("#888888"))
                }

                // Refresh intent
                val refreshIntent = Intent(context, ReelsWidgetProvider::class.java).apply {
                    action = ACTION_WIDGET_REFRESH
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
                }
                val pendingRefresh = PendingIntent.getBroadcast(
                    context,
                    widgetId,
                    refreshIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.refresh_stats, pendingRefresh)

                // Launch app on background click
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("target_tab", 1) // Stats tab
                }
                val pendingLaunch = PendingIntent.getActivity(
                    context,
                    widgetId + 1000,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.reels_bg, pendingLaunch)
            }

            appWidgetManager.updateAppWidget(widgetId, views)
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun formatCount(number: Long): String {
        if (number < 1000) return number.toString()
        val suffixes = arrayOf("", "k", "m")
        var value = number.toDouble()
        var idx = 0
        while (value >= 1000 && idx < suffixes.size - 1) {
            value /= 1000.0
            idx++
        }
        return if (value % 1.0 == 0.0) "${value.toInt()}${suffixes[idx]}"
        else "%.1f%s".format(value, suffixes[idx])
    }
}
