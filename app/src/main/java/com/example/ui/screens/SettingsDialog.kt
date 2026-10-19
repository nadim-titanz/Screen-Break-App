package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ReminderSettings
import com.example.service.CurrentAppDetector
import com.example.util.ReelsTrackingManager
import com.example.ui.ScreenBreakViewModel
import com.example.ui.theme.NothingBlack
import com.example.ui.theme.NothingOutline
import com.example.ui.theme.NothingOutlineVariant
import com.example.ui.theme.NothingRed
import com.example.ui.theme.NothingSurface
import com.example.ui.theme.NothingSurfaceElevated
import com.example.ui.theme.NothingTextDisabled
import com.example.ui.theme.NothingTextPrimary
import com.example.ui.theme.NothingTextSecondary
import com.example.ui.theme.NothingWhite

@Composable
fun SettingsDialog(
    viewModel: ScreenBreakViewModel,
    settings: ReminderSettings,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val intervalOptions = listOf(1, 2, 5, 10, 15, 20, 30)
    val usageState by viewModel.usageState.collectAsStateWithLifecycle()
    var showTurnOffConfirmDialog by rememberSaveable { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = NothingSurface,
            border = BorderStroke(1.dp, NothingOutline),
            tonalElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "// CONFIGURATION",
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                letterSpacing = 1.2.sp
                            ),
                            color = NothingRed
                        )
                        Text(
                            text = "SYSTEM SETTINGS",
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = NothingWhite
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = NothingTextSecondary
                        )
                    }
                }

                HorizontalDivider(color = NothingOutline)

                // Interval Section
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = NothingRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "CONTINUOUS USAGE INTERVAL",
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    ),
                                    color = NothingWhite
                                )
                                Text(
                                    text = if (usageState.isServiceRunning) "Active • ${usageState.intervalMinutes}m interval" else "Monitoring paused",
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontSize = 11.sp
                                    ),
                                    color = NothingTextSecondary
                                )
                            }
                        }

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
                            modifier = Modifier.testTag("settings_interval_toggle_switch")
                        )
                    }
                    Text(
                        text = "Continuous screen time threshold before triggering pop-up:",
                        style = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                        color = NothingTextSecondary
                    )

                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        intervalOptions.forEach { minutes ->
                            val isSelected = settings.intervalMinutes == minutes
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateInterval(minutes) },
                                label = {
                                    Text(
                                        "${minutes}m",
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(999.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NothingRed,
                                    selectedLabelColor = NothingWhite,
                                    containerColor = NothingSurfaceElevated,
                                    labelColor = NothingTextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = NothingOutline,
                                    selectedBorderColor = NothingRed,
                                    enabled = true,
                                    selected = isSelected
                                ),
                                modifier = Modifier.testTag("interval_chip_${minutes}m")
                            )
                        }
                    }
                }

                HorizontalDivider(color = NothingOutline)

                // Floating Overlay Pop-up Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = NothingWhite,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "FLOATING POP-UP WINDOW",
                                style = androidx.compose.ui.text.TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                ),
                                color = NothingWhite
                            )
                            Text(
                                text = "Display overlay across active apps",
                                style = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                                color = NothingTextSecondary
                            )
                        }
                    }
                    Switch(
                        checked = settings.overlayEnabled,
                        onCheckedChange = { viewModel.updateOverlay(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NothingWhite,
                            checkedTrackColor = NothingRed,
                            uncheckedThumbColor = NothingTextSecondary,
                            uncheckedTrackColor = NothingOutline
                        ),
                        modifier = Modifier.testTag("overlay_toggle_switch")
                    )
                }

                // Vibration Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = NothingWhite,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "HAPTIC VIBRATION",
                                style = androidx.compose.ui.text.TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                ),
                                color = NothingWhite
                            )
                            Text(
                                text = "Mechanical haptic pulse on pop-up",
                                style = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                                color = NothingTextSecondary
                            )
                        }
                    }
                    Switch(
                        checked = settings.vibrationEnabled,
                        onCheckedChange = { viewModel.updateVibration(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NothingWhite,
                            checkedTrackColor = NothingRed,
                            uncheckedThumbColor = NothingTextSecondary,
                            uncheckedTrackColor = NothingOutline
                        ),
                        modifier = Modifier.testTag("vibration_toggle_switch")
                    )
                }

                HorizontalDivider(color = NothingOutline)

                // Block Reels & Shorts Toggle
                var isBlockReelsOn by rememberSaveable { mutableStateOf(ReelsTrackingManager.isReelsBlockingEnabled(context)) }
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
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            ),
                            color = NothingWhite
                        )
                        Text(
                            text = "Auto-intercepts Instagram Reels & YT Shorts",
                            style = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                            color = NothingTextSecondary
                        )
                    }
                    Switch(
                        checked = isBlockReelsOn,
                        onCheckedChange = { checked ->
                            isBlockReelsOn = checked
                            ReelsTrackingManager.setReelsBlockingEnabled(context, checked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NothingWhite,
                            checkedTrackColor = NothingRed,
                            uncheckedThumbColor = NothingTextSecondary,
                            uncheckedTrackColor = NothingOutline
                        ),
                        modifier = Modifier.testTag("settings_block_reels_switch")
                    )
                }

                // Center Time HUD Toggle
                var isCenterHudOn by rememberSaveable { mutableStateOf(ReelsTrackingManager.isCenterTimerOverlayEnabled(context)) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TIME ELAPSED ON SCREEN CENTER",
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            ),
                            color = NothingWhite
                        )
                        Text(
                            text = "Shows live usage timer centered on screen",
                            style = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                            color = NothingTextSecondary
                        )
                    }
                    Switch(
                        checked = isCenterHudOn,
                        onCheckedChange = { checked ->
                            isCenterHudOn = checked
                            ReelsTrackingManager.setCenterTimerOverlayEnabled(context, checked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NothingWhite,
                            checkedTrackColor = NothingRed,
                            uncheckedThumbColor = NothingTextSecondary,
                            uncheckedTrackColor = NothingOutline
                        ),
                        modifier = Modifier.testTag("settings_center_hud_switch")
                    )
                }

                HorizontalDivider(color = NothingOutline)

                // System Permissions Links
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "// SYSTEM PREFERENCES",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        ),
                        color = NothingTextSecondary
                    )

                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = NothingSurfaceElevated),
                        border = BorderStroke(1.dp, NothingOutline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                context.startActivity(CurrentAppDetector.getUsageAccessIntent())
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Usage Access Settings",
                                style = androidx.compose.ui.text.TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 13.sp
                                ),
                                color = NothingWhite
                            )
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                tint = NothingTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = NothingSurfaceElevated),
                        border = BorderStroke(1.dp, NothingOutline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                context.startActivity(CurrentAppDetector.getOverlayPermissionIntent(context))
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Overlay Display Settings",
                                style = androidx.compose.ui.text.TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 13.sp
                                ),
                                color = NothingWhite
                            )
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                tint = NothingTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
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
