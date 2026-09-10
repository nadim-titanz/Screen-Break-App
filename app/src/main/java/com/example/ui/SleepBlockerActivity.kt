package com.example.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.ContinuousUsageService
import com.example.ui.theme.BrickBackground
import com.example.ui.theme.BrickOnPrimary
import com.example.ui.theme.BrickOutline
import com.example.ui.theme.BrickPrimary
import com.example.ui.theme.BrickSurface
import com.example.ui.theme.BrickSurfaceVariant
import com.example.ui.theme.BrickTextPrimary
import com.example.ui.theme.BrickTextSecondary
import com.example.ui.theme.MyApplicationTheme

class SleepBlockerActivity : ComponentActivity() {

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

        val scheduleTitle = intent.getStringExtra(EXTRA_SCHEDULE_TITLE) ?: "Sleep Schedule"
        val blockedPackage = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE) ?: ""
        val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: "This app"
        val timeRange = intent.getStringExtra(EXTRA_TIME_RANGE) ?: "Night Window"

        setContent {
            MyApplicationTheme {
                SleepBlockerScreen(
                    scheduleTitle = scheduleTitle,
                    blockedPackage = blockedPackage,
                    appName = appName,
                    timeRange = timeRange,
                    onEmergencyUse = {
                        ContinuousUsageService.startEmergencySnooze(this, blockedPackage)
                        finish()
                    },
                    onClose = {
                        ContinuousUsageService.kickToHomeScreen(this)
                        finish()
                    }
                )
            }
        }
    }

    override fun onBackPressed() {
        ContinuousUsageService.kickToHomeScreen(this)
        super.onBackPressed()
    }

    companion object {
        private const val EXTRA_SCHEDULE_TITLE = "extra_schedule_title"
        private const val EXTRA_BLOCKED_PACKAGE = "extra_blocked_package"
        private const val EXTRA_APP_NAME = "extra_app_name"
        private const val EXTRA_TIME_RANGE = "extra_time_range"

        fun start(
            context: Context,
            scheduleTitle: String,
            blockedPackage: String,
            appName: String,
            timeRange: String
        ) {
            val intent = Intent(context, SleepBlockerActivity::class.java).apply {
                putExtra(EXTRA_SCHEDULE_TITLE, scheduleTitle)
                putExtra(EXTRA_BLOCKED_PACKAGE, blockedPackage)
                putExtra(EXTRA_APP_NAME, appName)
                putExtra(EXTRA_TIME_RANGE, timeRange)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            context.startActivity(intent)
        }
    }
}

@Composable
fun SleepBlockerScreen(
    scheduleTitle: String,
    blockedPackage: String,
    appName: String,
    timeRange: String,
    onEmergencyUse: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("sleep_blocker_screen"),
        color = BrickBackground
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BrickBackground)
                .systemBarsPadding()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BrickSurface),
                border = BorderStroke(1.dp, BrickOutline),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 380.dp)
                    .testTag("sleep_blocker_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Minimal Icon Circle
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(BrickSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = "Sleep active",
                            tint = BrickTextPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Title & minimal description
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (appName.isNotBlank() && appName != "This app") "$appName is Paused" else "App Paused",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp,
                                letterSpacing = (-0.4).sp
                            ),
                            color = BrickTextPrimary,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = if (timeRange.isNotBlank()) "$scheduleTitle • $timeRange" else scheduleTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = BrickTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Action Buttons
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onClose,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrickPrimary,
                                contentColor = BrickOnPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("close_blocker_button")
                        ) {
                            Text(
                                text = "Close",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        OutlinedButton(
                            onClick = onEmergencyUse,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BrickOutline),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = BrickTextSecondary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("emergency_use_button")
                        ) {
                            Text(
                                text = "5-Minute Break",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
