package com.example.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ScreenBreakApplication
import com.example.service.ContinuousUsageService
import com.example.ui.components.ReminderOverlayContent
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class ReminderAlertActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        val continuousMinutes = intent.getIntExtra(EXTRA_MINUTES, 10)
        val activeAppName = intent.getStringExtra(EXTRA_APP_NAME) ?: ""
        val activePackage = intent.getStringExtra(EXTRA_PACKAGE) ?: ""

        val app = application as ScreenBreakApplication
        val repository = app.repository

        // Dismiss immediately if this app is ignored
        if (activePackage.isNotEmpty()) {
            kotlinx.coroutines.MainScope().launch {
                if (repository.isAppIgnored(activePackage)) {
                    finish()
                }
            }
        }

        setContent {
            MyApplicationTheme {
                val scope = rememberCoroutineScope()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            // Tap outside to dismiss
                            finish()
                        }
                        .systemBarsPadding(),
                    contentAlignment = Alignment.Center
                ) {
                    ReminderOverlayContent(
                        continuousMinutes = continuousMinutes,
                        activeAppName = activeAppName,
                        activePackage = activePackage,
                        onDismiss = {
                            val resetIntent = Intent(
                                this@ReminderAlertActivity,
                                ContinuousUsageService::class.java
                            ).apply {
                                action = ContinuousUsageService.ACTION_RESET
                                putExtra(ContinuousUsageService.EXTRA_LOCKED_PACKAGE, activePackage)
                                putExtra(ContinuousUsageService.EXTRA_LOCKED_APP_NAME, activeAppName)
                            }
                            startService(resetIntent)
                            ContinuousUsageService.kickToHomeScreen(this@ReminderAlertActivity)
                            finish()
                        },
                        onSnooze = {
                            ContinuousUsageService.sendAction(
                                this@ReminderAlertActivity,
                                ContinuousUsageService.ACTION_SNOOZE
                            )
                            scope.launch {
                                repository.logAction(
                                    continuousMinutes,
                                    activePackage,
                                    activeAppName,
                                    "SNOOZED"
                                )
                                finish()
                            }
                        },
                        modifier = Modifier
                            .padding(16.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                // Consume clicks inside card
                            }
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_MINUTES = "extra_minutes"
        const val EXTRA_APP_NAME = "extra_app_name"
        const val EXTRA_PACKAGE = "extra_package"

        fun createIntent(
            context: Context,
            minutes: Int,
            appName: String,
            packageName: String
        ): Intent {
            return Intent(context, ReminderAlertActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(EXTRA_MINUTES, minutes)
                putExtra(EXTRA_APP_NAME, appName)
                putExtra(EXTRA_PACKAGE, packageName)
            }
        }
    }
}
