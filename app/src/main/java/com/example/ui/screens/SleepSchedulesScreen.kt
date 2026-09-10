package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SleepScheduleEntity
import com.example.ui.ScreenBreakViewModel
import com.example.ui.theme.AppleSystemGreen
import com.example.ui.theme.SophisticatedBg
import com.example.ui.theme.SophisticatedCardContainer
import com.example.ui.theme.SophisticatedOutline
import com.example.ui.theme.SophisticatedOutlineVariant
import com.example.ui.theme.SophisticatedPrimary
import com.example.ui.theme.SophisticatedSurface
import com.example.ui.theme.SophisticatedSurfaceVariant
import com.example.ui.theme.SophisticatedTextMuted
import com.example.ui.theme.SophisticatedTextPrimary
import com.example.ui.theme.SophisticatedTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SleepSchedulesScreen(
    viewModel: ScreenBreakViewModel,
    modifier: Modifier = Modifier
) {
    val schedules by viewModel.sleepSchedules.collectAsStateWithLifecycle()
    val usageState by viewModel.usageState.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()

    var showEditorDialog by remember { mutableStateOf(false) }
    var editingSchedule by remember { mutableStateOf<SleepScheduleEntity?>(null) }
    var scheduleToDelete by remember { mutableStateOf<SleepScheduleEntity?>(null) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("sleep_schedules_screen"),
        containerColor = SophisticatedBg,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingSchedule = null
                    showEditorDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Schedule") },
                text = { Text("Add Schedule", fontWeight = FontWeight.Bold) },
                containerColor = SophisticatedPrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_new_schedule_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Header
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SophisticatedPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = null,
                                tint = SophisticatedPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "Sleep App Blocker",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = SophisticatedTextPrimary
                        )
                    }
                    Text(
                        text = "Enforce merciless app blocking to fix sleep hygiene. Only 5-minute emergency use allowed.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SophisticatedTextSecondary
                    )
                }
            }

            // Real-time Active Schedule Banner
            item {
                ActiveStatusBanner(
                    isActive = usageState.isSleepScheduleActive,
                    activeTitle = usageState.activeSleepScheduleTitle,
                    emergencySeconds = usageState.emergencySnoozeSecondsRemaining
                )
            }

            // Strict Rules Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceVariant.copy(alpha = 0.6f)),
                    border = BorderStroke(1.dp, SophisticatedOutline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = SophisticatedPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Schedule Rules",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = SophisticatedTextPrimary
                            )
                        }

                        Text(
                            text = "• App Restriction: Selected apps close immediately when opened during active hours.\n• 5-Min Emergency Pass: Request up to 5 minutes of temporary access if needed.\n• Multiple Schedules: Create separate profiles for sleep, work, or focus hours.",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            color = SophisticatedTextSecondary
                        )
                    }
                }
            }

            // Schedules Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Configured Schedules (${schedules.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = SophisticatedTextPrimary
                    )
                }
            }

            // Empty state
            if (schedules.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                        border = BorderStroke(1.dp, SophisticatedOutline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = null,
                                tint = SophisticatedTextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "No Sleep Schedules",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = SophisticatedTextPrimary
                            )
                            Text(
                                text = "Add your first schedule to start blocking sleep-disrupting apps at night.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SophisticatedTextMuted,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Button(
                                onClick = {
                                    editingSchedule = null
                                    showEditorDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SophisticatedPrimary),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Create Schedule", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(schedules, key = { it.id }) { schedule ->
                    ScheduleItemCard(
                        schedule = schedule,
                        onToggle = { isEnabled ->
                            viewModel.setScheduleEnabled(schedule.id, isEnabled)
                        },
                        onEdit = {
                            editingSchedule = schedule
                            showEditorDialog = true
                        },
                        onDelete = {
                            scheduleToDelete = schedule
                        }
                    )
                }
            }

            // Bottom spacer for FAB
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Schedule Editor Dialog
    if (showEditorDialog) {
        ScheduleEditorDialog(
            initialSchedule = editingSchedule,
            installedApps = installedApps,
            onDismiss = { showEditorDialog = false },
            onSave = { updatedSchedule ->
                if (editingSchedule == null) {
                    viewModel.saveSchedule(updatedSchedule)
                } else {
                    viewModel.updateSchedule(updatedSchedule)
                }
                showEditorDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    if (scheduleToDelete != null) {
        val target = scheduleToDelete!!
        AlertDialog(
            onDismissRequest = { scheduleToDelete = null },
            title = {
                Text(
                    text = "Delete Schedule?",
                    color = SophisticatedTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${target.title}\"? Apps will no longer be blocked during this time.",
                    color = SophisticatedTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSchedule(target.id)
                        scheduleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { scheduleToDelete = null }) {
                    Text("Cancel", color = SophisticatedTextSecondary)
                }
            },
            containerColor = SophisticatedSurface
        )
    }
}

@Composable
fun ActiveStatusBanner(
    isActive: Boolean,
    activeTitle: String,
    emergencySeconds: Int
) {
    if (isActive) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SophisticatedCardContainer),
            border = BorderStroke(1.5.dp, SophisticatedPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("active_sleep_banner")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                SophisticatedPrimary.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = null,
                            tint = SophisticatedPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "SCHEDULE ACTIVE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = SophisticatedPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SophisticatedPrimary.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Enforcing",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = SophisticatedPrimary
                        )
                    }
                }

                Text(
                    text = if (activeTitle.isNotEmpty()) activeTitle else "Sleep Schedule Running",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = SophisticatedTextPrimary
                )

                Text(
                    text = "Blocked apps will be immediately prevented from opening. Put your phone away and get some sleep!",
                    style = MaterialTheme.typography.bodySmall,
                    color = SophisticatedTextSecondary
                )

                AnimatedVisibility(visible = emergencySeconds > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE57373).copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFFB4AB),
                            modifier = Modifier.size(16.dp)
                        )
                        val mins = emergencySeconds / 60
                        val secs = emergencySeconds % 60
                        Text(
                            text = "5-Min Emergency Access Active: %02d:%02d remaining".format(mins, secs),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFFFB4AB)
                        )
                    }
                }
            }
        }
    } else {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
            border = BorderStroke(1.dp, SophisticatedOutline.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SophisticatedSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = SophisticatedTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sleep Blocker Armed",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = SophisticatedTextPrimary
                    )
                    Text(
                        text = "Schedules will automatically activate at their configured bedtime.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SophisticatedTextMuted
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScheduleItemCard(
    schedule: SleepScheduleEntity,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val blockedCount = schedule.getBlockedPackageSet().size

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (schedule.isEnabled) SophisticatedCardContainer else SophisticatedSurface
        ),
        border = BorderStroke(
            0.5.dp,
            if (schedule.isEnabled) SophisticatedPrimary.copy(alpha = 0.4f) else SophisticatedOutline
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("schedule_card_${schedule.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Row: Title, Time Range, and Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = schedule.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (schedule.isEnabled) SophisticatedTextPrimary else SophisticatedTextMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = schedule.formattedTimeRange(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = if (schedule.isEnabled) SophisticatedPrimary else SophisticatedTextMuted
                    )
                }

                Switch(
                    checked = schedule.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AppleSystemGreen,
                        uncheckedThumbColor = SophisticatedTextMuted,
                        uncheckedTrackColor = SophisticatedSurfaceVariant
                    ),
                    modifier = Modifier.testTag("schedule_switch_${schedule.id}")
                )
            }

            // Days of week
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                schedule.getDaysSet().forEach { day ->
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                text = day,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = SophisticatedSurfaceVariant,
                            labelColor = if (schedule.isEnabled) SophisticatedTextSecondary else SophisticatedTextMuted
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = SophisticatedOutline
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                }
            }

            // Blocked Apps Information
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SophisticatedSurfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (blockedCount > 0) SophisticatedPrimary else SophisticatedTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (blockedCount == 0) "No apps blocked" else "$blockedCount apps restricted",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = if (blockedCount > 0) SophisticatedTextPrimary else SophisticatedTextMuted
                    )
                }
            }

            // Action Buttons (Edit / Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("edit_schedule_${schedule.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Schedule",
                        tint = SophisticatedTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_schedule_${schedule.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Schedule",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
