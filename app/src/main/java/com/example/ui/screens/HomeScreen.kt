package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UsageState
import com.example.service.CurrentAppDetector
import com.example.ui.ScreenBreakViewModel
import com.example.ui.theme.AppleSystemGreen
import com.example.ui.theme.BrickAccentDot
import com.example.ui.theme.BrickBackground
import com.example.ui.theme.BrickBarFill
import com.example.ui.theme.BrickCardContainer
import com.example.ui.theme.BrickOnPrimary
import com.example.ui.theme.BrickOutline
import com.example.ui.theme.BrickPrimary
import com.example.ui.theme.BrickSurface
import com.example.ui.theme.BrickSurfaceVariant
import com.example.ui.theme.BrickTextMuted
import com.example.ui.theme.BrickTextPrimary
import com.example.ui.theme.BrickTextSecondary

@Composable
fun HomeScreen(
    viewModel: ScreenBreakViewModel,
    usageState: UsageState,
    reminderCount: Int,
    suppressedCount: Int,
    ignoredAppsCount: Int,
    hasUsageAccess: Boolean,
    hasOverlayPermission: Boolean,
    onNavigateToIgnored: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BrickBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header matching the Brick aesthetic
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CONTINUOUS SCREEN TIME",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp
                    ),
                    color = BrickTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (usageState.isServiceRunning) BrickAccentDot else BrickTextMuted)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (usageState.isServiceRunning) "Active • ${usageState.intervalMinutes}m periodic nudge" else "Monitoring paused",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                        color = BrickTextSecondary
                    )
                }
            }

            // Service Toggle Switch (Apple iOS style green active switch)
            Switch(
                checked = usageState.isServiceRunning,
                onCheckedChange = { viewModel.toggleService() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AppleSystemGreen,
                    uncheckedThumbColor = BrickTextSecondary,
                    uncheckedTrackColor = BrickOutline
                ),
                modifier = Modifier.testTag("service_toggle_switch")
            )
        }

        // Live Timer Card with Circular Dial
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = BrickSurface),
            border = BorderStroke(0.5.dp, BrickOutline),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular Progress Dial
                val animatedProgress by animateFloatAsState(
                    targetValue = usageState.progress,
                    label = "progress"
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(168.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = BrickOutline,
                        strokeWidth = 10.dp
                    )

                    val progressColor = if (usageState.isCurrentAppIgnored) {
                        BrickTextMuted
                    } else {
                        BrickBarFill
                    }

                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = progressColor,
                        strokeWidth = 10.dp
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = usageState.formattedTime,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 36.sp,
                                letterSpacing = (-1.2).sp
                            ),
                            color = BrickTextPrimary
                        )
                        Text(
                            text = "UNTIL BREAK",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                letterSpacing = 1.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = BrickTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Active App Status Pill
                if (usageState.activeAppName.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = BrickSurfaceVariant,
                        border = BorderStroke(1.dp, BrickOutline),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (usageState.isCurrentAppIgnored) Icons.Default.Block else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = if (usageState.isCurrentAppIgnored) BrickTextMuted else BrickBarFill,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (usageState.isCurrentAppIgnored) {
                                    "${usageState.activeAppName} (Ignored • No Pop-up)"
                                } else {
                                    "Active: ${usageState.activeAppName}"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                ),
                                color = BrickTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.testReminderPopUp() },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("test_popup_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrickBarFill,
                            contentColor = BrickOnPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Test Pop-up",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.resetTimer() },
                        modifier = Modifier
                            .weight(0.7f)
                            .height(46.dp)
                            .testTag("reset_streak_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BrickOutline),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = BrickTextPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset", fontSize = 14.sp)
                    }
                }
            }
        }

        // 30-Second Lockout Card (Strict Dopamine Detox)
        AnimatedVisibility(
            visible = usageState.lockedSecondsRemaining > 0
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BrickSurface),
                border = BorderStroke(1.5.dp, BrickBarFill),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("locked_app_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(BrickBarFill.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = BrickBarFill,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "30s Break in Progress",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = BrickTextPrimary
                        )
                        Text(
                            text = "${usageState.lockedAppName.ifEmpty { "Apps" }} locked for ${usageState.lockedSecondsRemaining}s",
                            style = MaterialTheme.typography.bodySmall,
                            color = BrickTextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(BrickBarFill)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${usageState.lockedSecondsRemaining}s",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = BrickOnPrimary
                        )
                    }
                }
            }
        }

        // Permissions Warnings / Action cards
        if (!hasUsageAccess || !hasOverlayPermission) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BrickSurface),
                border = BorderStroke(1.dp, BrickOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = BrickBarFill
                        )
                        Text(
                            text = "Recommended Permissions",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = BrickTextPrimary
                        )
                    }

                    if (!hasUsageAccess) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = "Usage Access",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = BrickTextPrimary
                                )
                                Text(
                                    text = "Detect active apps to suppress reminders during ignored apps.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BrickTextSecondary
                                )
                            }
                            Button(
                                onClick = {
                                    context.startActivity(CurrentAppDetector.getUsageAccessIntent())
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrickBarFill,
                                    contentColor = BrickOnPrimary
                                ),
                                modifier = Modifier.testTag("grant_usage_access_button")
                            ) {
                                Text("Grant", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    if (!hasOverlayPermission) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = "Display Over Other Apps",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = BrickTextPrimary
                                )
                                Text(
                                    text = "Allows the break reminder pop-up to show directly on screen.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BrickTextSecondary
                                )
                            }
                            Button(
                                onClick = {
                                    context.startActivity(CurrentAppDetector.getOverlayPermissionIntent(context))
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrickBarFill,
                                    contentColor = BrickOnPrimary
                                ),
                                modifier = Modifier.testTag("grant_overlay_permission_button")
                            ) {
                                Text("Allow", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // Quick Statistics Cards in Brick card style
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BrickSurface),
                border = BorderStroke(1.dp, BrickOutline),
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$reminderCount",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = BrickTextPrimary
                    )
                    Text(
                        text = "Alerts Sent",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = BrickTextSecondary
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BrickSurface),
                border = BorderStroke(1.dp, BrickOutline),
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$suppressedCount",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = BrickBarFill
                    )
                    Text(
                        text = "Suppressed",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = BrickTextSecondary
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BrickSurface),
                border = BorderStroke(1.dp, BrickOutline),
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp)
                    .clickable { onNavigateToIgnored() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$ignoredAppsCount",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = BrickTextPrimary
                    )
                    Text(
                        text = "Ignored Apps",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = BrickTextSecondary
                    )
                }
            }
        }

        // How it works info card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BrickSurface),
            border = BorderStroke(1.dp, BrickOutline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BrickCardContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SelfImprovement,
                        contentDescription = null,
                        tint = BrickBarFill
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "How Interrupt Works",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = BrickTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Every 10 minutes of continuous phone usage, you'll receive a pop-up break reminder. If you're using an app you added to your 'Ignored Apps' list, the reminder will stay quiet.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        ),
                        color = BrickTextSecondary
                    )
                }
            }
        }
    }
}
