package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UsageState
import com.example.service.CurrentAppDetector
import com.example.service.SleepNotificationBlockerService
import com.example.ui.ScreenBreakViewModel
import com.example.ui.components.NothingChronographClock
import com.example.util.ReelsTrackingManager
import com.example.ui.theme.NothingBlack
import com.example.ui.theme.NothingCardContainer
import com.example.ui.theme.NothingOutline
import com.example.ui.theme.NothingOutlineVariant
import com.example.ui.theme.NothingRed
import com.example.ui.theme.NothingSurface
import com.example.ui.theme.NothingSurfaceElevated
import com.example.ui.theme.NothingTextDisabled
import com.example.ui.theme.NothingTextMuted
import com.example.ui.theme.NothingTextPrimary
import com.example.ui.theme.NothingTextSecondary
import com.example.ui.theme.NothingWhite

@Composable
fun HomeScreen(
    viewModel: ScreenBreakViewModel,
    usageState: UsageState,
    reminderCount: Int,
    suppressedCount: Int,
    ignoredAppsCount: Int,
    hasUsageAccess: Boolean,
    hasOverlayPermission: Boolean,
    hasNotificationAccess: Boolean = true,
    onNavigateToStats: () -> Unit = {},
    onNavigateToIgnored: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showTurnOffConfirmDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NothingBlack)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Technical Header & Service Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "// CONTINUOUS USAGE",
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp
                    ),
                    color = NothingTextSecondary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (usageState.isServiceRunning) NothingRed else NothingTextMuted)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (usageState.isServiceRunning) {
                            "ACTIVE • ${usageState.intervalMinutes}M NUDGE"
                        } else {
                            "MONITORING PAUSED"
                        },
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = if (usageState.isServiceRunning) NothingWhite else NothingTextMuted
                    )
                }
            }

            // Technical Mechanical Switch
            Switch(
                checked = usageState.isServiceRunning,
                onCheckedChange = { checked ->
                    if (!checked) {
                        showTurnOffConfirmDialog = true
                    } else {
                        viewModel.startService()
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NothingWhite,
                    checkedTrackColor = NothingRed,
                    uncheckedThumbColor = NothingTextSecondary,
                    uncheckedTrackColor = NothingOutline
                ),
                modifier = Modifier.testTag("service_toggle_switch")
            )
        }

        // Hero Chronograph Clock Dial (Nothing Design)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NothingSurface),
            border = BorderStroke(1.dp, NothingOutline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // The Chronograph Clock Centerpiece
                NothingChronographClock(
                    progress = if (usageState.isServiceRunning) usageState.progress else 0f,
                    timeText = usageState.formattedTime,
                    unitText = "min",
                    dialSize = 250.dp,
                    isActive = usageState.isServiceRunning
                )

                // Sub-readout & Target Indicator
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(NothingSurfaceElevated)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(
                                if (usageState.isCurrentAppIgnored) NothingTextMuted
                                else if (usageState.isServiceRunning) NothingRed
                                else NothingTextSecondary
                            )
                    )
                    Text(
                        text = if (!usageState.isServiceRunning) {
                            "MONITOR OFF"
                        } else if (usageState.isCurrentAppIgnored) {
                            "EXCLUDED APP (${usageState.activeAppName})"
                        } else {
                            "TARGET: ${usageState.formattedTarget} // BREAK AT ${usageState.intervalMinutes}M"
                        },
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = NothingTextPrimary
                    )
                }

                // Active App Status Indicator
                if (usageState.activeAppName.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(NothingBlack)
                            .border(BorderStroke(1.dp, NothingOutline), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (usageState.isCurrentAppIgnored) Icons.Default.Block else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = if (usageState.isCurrentAppIgnored) NothingTextMuted else NothingRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "ACTIVE APP",
                                style = androidx.compose.ui.text.TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp
                                ),
                                color = NothingTextSecondary
                            )
                        }
                        Text(
                            text = if (usageState.isCurrentAppIgnored) "${usageState.activeAppName} [IGNORED]" else usageState.activeAppName,
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            ),
                            color = if (usageState.isCurrentAppIgnored) NothingTextMuted else NothingWhite
                        )
                    }
                }

                // Action Controls: Pill Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.testReminderPopUp() },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("test_popup_button"),
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NothingRed,
                            contentColor = NothingWhite
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "TEST NUDGE",
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.resetTimer() },
                        modifier = Modifier
                            .weight(0.8f)
                            .height(44.dp)
                            .testTag("reset_streak_button"),
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, NothingOutline),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = NothingWhite
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "RESET",
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }
            }
        }

        // Reels & Short-form Detox Card (from digipaws feature)
        val reelsToday by ReelsTrackingManager.todayReelsFlow.collectAsState()
        LaunchedEffect(Unit) {
            ReelsTrackingManager.getTodayCount(context)
        }
        var isReelsBlocked by rememberSaveable { mutableStateOf(ReelsTrackingManager.isReelsBlockingEnabled(context)) }
        var isCenterHudActive by rememberSaveable { mutableStateOf(ReelsTrackingManager.isCenterTimerOverlayEnabled(context)) }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NothingSurface),
            border = BorderStroke(1.dp, if (isReelsBlocked) NothingRed else NothingOutline),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reels_quick_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isReelsBlocked) NothingRed else NothingWhite)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "REELS & SHORTS // DETOX",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        ),
                        color = if (isReelsBlocked) NothingRed else NothingWhite
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "$reelsToday SCROLLED",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = NothingTextSecondary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "BLOCK REELS & SHORTS",
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = NothingWhite
                        )
                        Text(
                            text = "Instagram, YouTube Shorts & TikTok",
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            ),
                            color = NothingTextSecondary
                        )
                    }
                    Switch(
                        checked = isReelsBlocked,
                        onCheckedChange = { checked ->
                            isReelsBlocked = checked
                            ReelsTrackingManager.setReelsBlockingEnabled(context, checked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NothingWhite,
                            checkedTrackColor = NothingRed,
                            uncheckedThumbColor = NothingTextMuted,
                            uncheckedTrackColor = NothingBlack,
                            uncheckedBorderColor = NothingOutline
                        ),
                        modifier = Modifier.testTag("home_switch_reels_block")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CENTER SCREEN TIME HUD",
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = NothingWhite
                        )
                        Text(
                            text = "Live overlay timer centered in apps",
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            ),
                            color = NothingTextSecondary
                        )
                    }
                    Switch(
                        checked = isCenterHudActive,
                        onCheckedChange = { checked ->
                            isCenterHudActive = checked
                            ReelsTrackingManager.setCenterTimerOverlayEnabled(context, checked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NothingWhite,
                            checkedTrackColor = NothingRed,
                            uncheckedThumbColor = NothingTextMuted,
                            uncheckedTrackColor = NothingBlack,
                            uncheckedBorderColor = NothingOutline
                        ),
                        modifier = Modifier.testTag("home_switch_center_hud")
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NothingBlack.copy(alpha = 0.6f))
                        .clickable { onNavigateToStats() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VIEW DETAILED APP USAGE STATS",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = NothingWhite,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "→",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = NothingRed
                    )
                }
            }
        }

        // 30-Second Lockout Card (Strict Dopamine Detox)
        AnimatedVisibility(
            visible = usageState.lockedSecondsRemaining > 0
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NothingSurface),
                border = BorderStroke(1.5.dp, NothingRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("locked_app_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(NothingRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = NothingRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DETOX LOCKOUT ACTIVE",
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = NothingWhite
                        )
                        Text(
                            text = "${usageState.lockedAppName.ifEmpty { "Apps" }} locked for ${usageState.lockedSecondsRemaining}s",
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 12.sp
                            ),
                            color = NothingTextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NothingRed)
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${usageState.lockedSecondsRemaining}s",
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = NothingWhite
                        )
                    }
                }
            }
        }

        // Permissions Warnings / Action cards
        if (!hasUsageAccess || !hasOverlayPermission || !hasNotificationAccess) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NothingSurface),
                border = BorderStroke(1.dp, NothingOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = NothingRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "// PERMISSION REQUIREMENTS",
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            ),
                            color = NothingWhite
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
                                    text = "USAGE ACCESS",
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    ),
                                    color = NothingWhite
                                )
                                Text(
                                    text = "Detect active applications to suppress reminders during ignored apps.",
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    ),
                                    color = NothingTextSecondary
                                )
                            }
                            Button(
                                onClick = {
                                    context.startActivity(CurrentAppDetector.getUsageAccessIntent())
                                },
                                shape = RoundedCornerShape(999.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NothingRed,
                                    contentColor = NothingWhite
                                ),
                                modifier = Modifier.testTag("grant_usage_access_button")
                            ) {
                                Text(
                                    "GRANT",
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
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
                                    text = "OVERLAY PERMISSION",
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    ),
                                    color = NothingWhite
                                )
                                Text(
                                    text = "Allows full-screen break reminder nudges to show over running apps.",
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    ),
                                    color = NothingTextSecondary
                                )
                            }
                            Button(
                                onClick = {
                                    context.startActivity(CurrentAppDetector.getOverlayPermissionIntent(context))
                                },
                                shape = RoundedCornerShape(999.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NothingRed,
                                    contentColor = NothingWhite
                                ),
                                modifier = Modifier.testTag("grant_overlay_permission_button")
                            ) {
                                Text(
                                    "ALLOW",
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    if (!hasNotificationAccess) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = "NOTIFICATION SHIELD",
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    ),
                                    color = NothingWhite
                                )
                                Text(
                                    text = "Suppresses distracting notifications from apps during active sleep schedules.",
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    ),
                                    color = NothingTextSecondary
                                )
                            }
                            Button(
                                onClick = {
                                    context.startActivity(SleepNotificationBlockerService.getNotificationAccessIntent(context))
                                },
                                shape = RoundedCornerShape(999.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NothingRed,
                                    contentColor = NothingWhite
                                ),
                                modifier = Modifier.testTag("grant_notification_access_button")
                            ) {
                                Text(
                                    "ENABLE",
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Technical Statistics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NothingSurface),
                border = BorderStroke(1.dp, NothingOutline),
                modifier = Modifier
                    .weight(1f)
                    .height(86.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$reminderCount",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Light,
                            fontSize = 24.sp
                        ),
                        color = NothingWhite
                    )
                    Text(
                        text = "ALERTS",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        ),
                        color = NothingTextSecondary
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NothingSurface),
                border = BorderStroke(1.dp, NothingOutline),
                modifier = Modifier
                    .weight(1f)
                    .height(86.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$suppressedCount",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Light,
                            fontSize = 24.sp
                        ),
                        color = NothingRed
                    )
                    Text(
                        text = "SUPPRESSED",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        ),
                        color = NothingTextSecondary
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NothingSurface),
                border = BorderStroke(1.dp, NothingOutline),
                modifier = Modifier
                    .weight(1f)
                    .height(86.dp)
                    .clickable { onNavigateToIgnored() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$ignoredAppsCount",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Light,
                            fontSize = 24.sp
                        ),
                        color = NothingWhite
                    )
                    Text(
                        text = "EXCLUSIONS",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        ),
                        color = NothingTextSecondary
                    )
                }
            }
        }

        // Philosophy / Mechanism Info Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = NothingSurface),
            border = BorderStroke(1.dp, NothingOutline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(NothingSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = NothingWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "INTENTIONAL INTERRUPT SYSTEM",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = NothingWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Every ${usageState.intervalMinutes} minutes of active screen time generates an intentional interruption nudge to reset your awareness. Excluded apps operate quietly without triggering reminders.",
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        ),
                        color = NothingTextSecondary
                    )
                }
            }
        }
    }

    if (showTurnOffConfirmDialog) {
        TurnOffIntervalConfirmDialog(
            onDismiss = { showTurnOffConfirmDialog = false },
            onConfirm = {
                showTurnOffConfirmDialog = false
                viewModel.stopService()
            }
        )
    }
}
