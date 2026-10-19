package com.example.ui.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.util.DeviceUsageStatsHelper

class ScreentimeWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_WIDGET_REFRESH = "com.example.screentime.WIDGET_REFRESH"
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
                    android.content.ComponentName(context, ScreentimeWidgetProvider::class.java)
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
            val appUsageList = DeviceUsageStatsHelper.getTodayAppUsageList(context)
            val totalMinutes = appUsageList.sumOf { it.usageMinutes }
            val formattedTotal = DeviceUsageStatsHelper.formatDuration(totalMinutes)

            val views = RemoteViews(context.packageName, R.layout.widget_app_stats).apply {
                setTextViewText(R.id.screentime_widget, formattedTotal)

                // Set top 3 apps
                val top1 = appUsageList.getOrNull(0)
                val top2 = appUsageList.getOrNull(1)
                val top3 = appUsageList.getOrNull(2)

                setTextViewText(
                    R.id.app_1_sm,
                    if (top1 != null) "${DeviceUsageStatsHelper.formatDuration(top1.usageMinutes)} : ${top1.appName}" else "--"
                )
                setTextViewText(
                    R.id.app_2_sm,
                    if (top2 != null) "${DeviceUsageStatsHelper.formatDuration(top2.usageMinutes)} : ${top2.appName}" else "--"
                )
                setTextViewText(
                    R.id.app_3_sm,
                    if (top3 != null) "${DeviceUsageStatsHelper.formatDuration(top3.usageMinutes)} : ${top3.appName}" else "--"
                )

                // Refresh button
                val refreshIntent = Intent(context, ScreentimeWidgetProvider::class.java).apply {
                    action = ACTION_WIDGET_REFRESH
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
                }
                val pendingRefresh = PendingIntent.getBroadcast(
                    context,
                    widgetId,
                    refreshIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.refresh_stats_screentime, pendingRefresh)

                // Launch app on background click
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("target_tab", 1) // Stats tab
                }
                val pendingLaunch = PendingIntent.getActivity(
                    context,
                    widgetId + 2000,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.widget_bg_app_stats, pendingLaunch)
            }

            appWidgetManager.updateAppWidget(widgetId, views)
        } catch (e: Exception) {
            // ignore
        }
    }
}
