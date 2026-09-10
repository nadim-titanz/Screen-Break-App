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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.ReminderSettings
import com.example.service.CurrentAppDetector
import com.example.ui.ScreenBreakViewModel
import com.example.ui.theme.SophisticatedCardContainer
import com.example.ui.theme.SophisticatedOnPrimary
import com.example.ui.theme.SophisticatedOutline
import com.example.ui.theme.SophisticatedPrimary
import com.example.ui.theme.SophisticatedSurface
import com.example.ui.theme.SophisticatedSurfaceVariant
import com.example.ui.theme.SophisticatedTextPrimary
import com.example.ui.theme.SophisticatedTextSecondary

@Composable
fun SettingsDialog(
    viewModel: ScreenBreakViewModel,
    settings: ReminderSettings,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val intervalOptions = listOf(1, 2, 5, 10, 15, 20, 30)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = SophisticatedSurface,
            border = BorderStroke(1.dp, SophisticatedOutline),
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reminder Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = (-0.5).sp
                        ),
                        color = SophisticatedTextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = SophisticatedTextSecondary
                        )
                    }
                }

                HorizontalDivider(color = SophisticatedOutline)

                // Interval Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = SophisticatedPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Continuous Usage Interval",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = SophisticatedTextPrimary
                        )
                    }
                    Text(
                        text = "Continuous screen time threshold before triggering pop-up (1 min to 30 min):",
                        style = MaterialTheme.typography.bodySmall,
                        color = SophisticatedTextSecondary
                    )

                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        intervalOptions.forEach { minutes ->
                            FilterChip(
                                selected = settings.intervalMinutes == minutes,
                                onClick = { viewModel.updateInterval(minutes) },
                                label = { Text("${minutes}m") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SophisticatedCardContainer,
                                    selectedLabelColor = SophisticatedPrimary,
                                    containerColor = SophisticatedSurfaceVariant,
                                    labelColor = SophisticatedTextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = SophisticatedOutline,
                                    selectedBorderColor = SophisticatedPrimary,
                                    enabled = true,
                                    selected = settings.intervalMinutes == minutes
                                ),
                                modifier = Modifier.testTag("interval_chip_${minutes}m")
                            )
                        }
                    }
                }

                HorizontalDivider(color = SophisticatedOutline)

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
                            tint = SophisticatedPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Floating Pop-up Window",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = SophisticatedTextPrimary
                            )
                            Text(
                                text = "Display overlay pop-up across active apps",
                                style = MaterialTheme.typography.bodySmall,
                                color = SophisticatedTextSecondary
                            )
                        }
                    }
                    Switch(
                        checked = settings.overlayEnabled,
                        onCheckedChange = { viewModel.updateOverlay(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SophisticatedOnPrimary,
                            checkedTrackColor = SophisticatedPrimary,
                            uncheckedThumbColor = SophisticatedTextSecondary,
                            uncheckedTrackColor = SophisticatedOutline
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
                            tint = SophisticatedPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Vibration",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = SophisticatedTextPrimary
                            )
                            Text(
                                text = "Vibrate on break pop-up",
                                style = MaterialTheme.typography.bodySmall,
                                color = SophisticatedTextSecondary
                            )
                        }
                    }
                    Switch(
                        checked = settings.vibrationEnabled,
                        onCheckedChange = { viewModel.updateVibration(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SophisticatedOnPrimary,
                            checkedTrackColor = SophisticatedPrimary,
                            uncheckedThumbColor = SophisticatedTextSecondary,
                            uncheckedTrackColor = SophisticatedOutline
                        ),
                        modifier = Modifier.testTag("vibration_toggle_switch")
                    )
                }

                HorizontalDivider(color = SophisticatedOutline)

                // System Permissions Links
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "System Settings",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = SophisticatedTextPrimary
                    )

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceVariant),
                        border = BorderStroke(1.dp, SophisticatedOutline),
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
                                style = MaterialTheme.typography.bodyMedium,
                                color = SophisticatedTextPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                tint = SophisticatedTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceVariant),
                        border = BorderStroke(1.dp, SophisticatedOutline),
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
                                text = "Display Over Other Apps Settings",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SophisticatedTextPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                tint = SophisticatedTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Developed and designed by Nadim",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.2.sp
                    ),
                    color = SophisticatedTextSecondary.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("developed_by_text")
                )
            }
        }
    }
}
