package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SleepScheduleEntity
import com.example.ui.ScreenBreakViewModel
import com.example.ui.theme.NothingBlack
import com.example.ui.theme.NothingOutline
import com.example.ui.theme.NothingRed
import com.example.ui.theme.NothingSurface
import com.example.ui.theme.NothingTextMuted
import com.example.ui.theme.NothingTextSecondary
import com.example.ui.theme.NothingWhite
import java.util.Calendar

@Composable
fun SleepSchedulesScreen(
    viewModel: ScreenBreakViewModel,
    modifier: Modifier = Modifier
) {
    val schedules by viewModel.sleepSchedules.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    var editingSchedule by remember { mutableStateOf<SleepScheduleEntity?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = NothingBlack,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isCreatingNew = true },
                containerColor = NothingRed,
                contentColor = NothingWhite,
                shape = CircleShape,
                modifier = Modifier.testTag("add_schedule_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Schedule"
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Technical Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NothingRed)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SCHEDULE PROTOCOL",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp
                    ),
                    color = NothingRed
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "[${schedules.count { it.isEnabled }} ACTIVE]",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    ),
                    color = NothingTextMuted
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "SLEEP & DETOX",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    letterSpacing = 0.5.sp
                ),
                color = NothingWhite
            )

            Text(
                text = "Automated distraction lock during rest hours",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                ),
                color = NothingTextSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (schedules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NO SCHEDULES CONFIGURED",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = NothingTextMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap + to initialize a sleep protocol",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            ),
                            color = NothingTextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(schedules, key = { it.id }) { schedule ->
                        ScheduleCard(
                            schedule = schedule,
                            onToggle = { isEnabled ->
                                viewModel.setScheduleEnabled(schedule.id, isEnabled)
                            },
                            onEdit = { editingSchedule = schedule },
                            onDelete = { viewModel.deleteSchedule(schedule.id) }
                        )
                    }
                }
            }
        }
    }

    if (isCreatingNew) {
        ScheduleEditorDialog(
            initialSchedule = null,
            installedApps = installedApps,
            onDismiss = { isCreatingNew = false },
            onSave = { newSchedule ->
                viewModel.saveSchedule(newSchedule)
                isCreatingNew = false
            }
        )
    }

    editingSchedule?.let { scheduleToEdit ->
        ScheduleEditorDialog(
            initialSchedule = scheduleToEdit,
            installedApps = installedApps,
            onDismiss = { editingSchedule = null },
            onSave = { updatedSchedule ->
                viewModel.updateSchedule(updatedSchedule)
                editingSchedule = null
            }
        )
    }
}

@Composable
fun ScheduleCard(
    schedule: SleepScheduleEntity,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isCurrentlyActive = remember(schedule) {
        isScheduleNow(schedule)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NothingSurface)
            .border(
                BorderStroke(
                    1.dp,
                    if (isCurrentlyActive && schedule.isEnabled) NothingRed else NothingOutline
                ),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
            .testTag("schedule_card_${schedule.id}")
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = schedule.title.uppercase(),
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = NothingWhite
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    if (isCurrentlyActive && schedule.isEnabled) {
                        Text(
                            text = "● IN PROGRESS // RESTRICTED",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = NothingRed
                        )
                    } else {
                        Text(
                            text = if (schedule.isEnabled) "ACTIVE PROTOCOL" else "OFFLINE",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            ),
                            color = NothingTextMuted
                        )
                    }
                }

                Switch(
                    checked = schedule.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NothingWhite,
                        checkedTrackColor = NothingRed,
                        uncheckedThumbColor = NothingTextMuted,
                        uncheckedTrackColor = NothingBlack,
                        uncheckedBorderColor = NothingOutline
                    ),
                    modifier = Modifier.testTag("switch_schedule_${schedule.id}")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time range display
            val startFormatted = String.format("%02d:%02d", schedule.startHour, schedule.startMinute)
            val endFormatted = String.format("%02d:%02d", schedule.endHour, schedule.endMinute)

            Text(
                text = "$startFormatted → $endFormatted",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    letterSpacing = 1.sp
                ),
                color = if (schedule.isEnabled) NothingWhite else NothingTextMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Days badges
            val activeDays = schedule.daysOfWeek.split(",").map { it.trim().uppercase() }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN").forEach { day ->
                    val isDayActive = activeDays.contains(day)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isDayActive && schedule.isEnabled) NothingRed.copy(alpha = 0.2f)
                                else Color.Transparent
                            )
                            .border(
                                BorderStroke(
                                    0.5.dp,
                                    if (isDayActive && schedule.isEnabled) NothingRed else NothingOutline
                                ),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = day.take(1),
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isDayActive) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 9.sp
                            ),
                            color = if (isDayActive && schedule.isEnabled) NothingWhite else NothingTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(28.dp).testTag("edit_schedule_${schedule.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Schedule",
                        tint = NothingTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp).testTag("delete_schedule_${schedule.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Schedule",
                        tint = NothingTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private fun isScheduleNow(schedule: SleepScheduleEntity): Boolean {
    val cal = Calendar.getInstance()
    val currentMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    val startMinutes = schedule.startHour * 60 + schedule.startMinute
    val endMinutes = schedule.endHour * 60 + schedule.endMinute

    return if (startMinutes <= endMinutes) {
        currentMinutes in startMinutes..endMinutes
    } else {
        currentMinutes >= startMinutes || currentMinutes <= endMinutes
    }
}
