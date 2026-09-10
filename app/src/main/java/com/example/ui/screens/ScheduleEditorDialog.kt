package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import com.example.data.SleepScheduleEntity
import com.example.model.AppItem
import com.example.ui.theme.SophisticatedCardContainer
import com.example.ui.theme.SophisticatedOutline
import com.example.ui.theme.SophisticatedPrimary
import com.example.ui.theme.SophisticatedSurface
import com.example.ui.theme.SophisticatedTextMuted
import com.example.ui.theme.SophisticatedTextPrimary
import com.example.ui.theme.SophisticatedTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScheduleEditorDialog(
    initialSchedule: SleepScheduleEntity? = null,
    installedApps: List<AppItem>,
    onDismiss: () -> Unit,
    onSave: (SleepScheduleEntity) -> Unit
) {
    var title by rememberSaveable { mutableStateOf(initialSchedule?.title ?: "Night Sleep") }
    var startHour by rememberSaveable { mutableIntStateOf(initialSchedule?.startHour ?: 23) }
    var startMinute by rememberSaveable { mutableIntStateOf(initialSchedule?.startMinute ?: 0) }
    var endHour by rememberSaveable { mutableIntStateOf(initialSchedule?.endHour ?: 7) }
    var endMinute by rememberSaveable { mutableIntStateOf(initialSchedule?.endMinute ?: 0) }

    var selectedDays by remember {
        mutableStateOf(
            initialSchedule?.getDaysSet() ?: setOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
        )
    }

    var selectedPackages by remember {
        mutableStateOf(initialSchedule?.getBlockedPackageSet() ?: emptySet())
    }

    var appSearchQuery by remember { mutableStateOf("") }

    val filteredApps = remember(installedApps, appSearchQuery) {
        if (appSearchQuery.isBlank()) {
            installedApps
        } else {
            installedApps.filter {
                it.appName.contains(appSearchQuery, ignoreCase = true) ||
                    it.packageName.contains(appSearchQuery, ignoreCase = true)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
            border = BorderStroke(1.dp, SophisticatedOutline),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 16.dp)
                .testTag("schedule_editor_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SophisticatedPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = null,
                                tint = SophisticatedPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = if (initialSchedule == null) "New Sleep Schedule" else "Edit Schedule",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = SophisticatedTextPrimary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_editor_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = SophisticatedTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Schedule Name
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Schedule Name",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = SophisticatedTextSecondary
                            )
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                placeholder = { Text("e.g. Night Sleep, Bedtime Wind-down") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("schedule_title_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SophisticatedPrimary,
                                    unfocusedBorderColor = SophisticatedOutline,
                                    focusedTextColor = SophisticatedTextPrimary,
                                    unfocusedTextColor = SophisticatedTextPrimary
                                )
                            )
                        }
                    }

                    // Time Window Pickers
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Blocking Window",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = SophisticatedTextSecondary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Start Time
                                TimeAdjusterCard(
                                    label = "Start (Bedtime)",
                                    hour = startHour,
                                    minute = startMinute,
                                    modifier = Modifier.weight(1f),
                                    onTimeChange = { h, m ->
                                        startHour = h
                                        startMinute = m
                                    }
                                )

                                // End Time
                                TimeAdjusterCard(
                                    label = "End (Wake up)",
                                    hour = endHour,
                                    minute = endMinute,
                                    modifier = Modifier.weight(1f),
                                    onTimeChange = { h, m ->
                                        endHour = h
                                        endMinute = m
                                    }
                                )
                            }
                        }
                    }

                    // Days of Week
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Active Days",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = SophisticatedTextSecondary
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "All",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = SophisticatedPrimary,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable {
                                                selectedDays = SleepScheduleEntity.ALL_DAYS.toSet()
                                            }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                    Text(
                                        text = "Weekdays",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = SophisticatedPrimary,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable {
                                                selectedDays = SleepScheduleEntity.WEEKDAYS.toSet()
                                            }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                SleepScheduleEntity.ALL_DAYS.forEach { day ->
                                    val isSelected = selectedDays.contains(day)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedDays = if (isSelected) {
                                                if (selectedDays.size > 1) selectedDays - day else selectedDays
                                            } else {
                                                selectedDays + day
                                            }
                                        },
                                        label = { Text(day, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SophisticatedPrimary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                            containerColor = SophisticatedCardContainer,
                                            labelColor = SophisticatedTextSecondary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Blocked Apps Selector
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Apps,
                                        contentDescription = null,
                                        tint = SophisticatedPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Blocked Apps (${selectedPackages.size} selected)",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = SophisticatedTextPrimary
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Select All",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = SophisticatedPrimary,
                                        modifier = Modifier
                                            .clickable {
                                                selectedPackages = installedApps.map { it.packageName }.toSet()
                                            }
                                            .padding(4.dp)
                                    )
                                    Text(
                                        text = "Clear",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = SophisticatedTextMuted,
                                        modifier = Modifier
                                            .clickable {
                                                selectedPackages = emptySet()
                                            }
                                            .padding(4.dp)
                                    )
                                }
                            }

                            // Search Field
                            OutlinedTextField(
                                value = appSearchQuery,
                                onValueChange = { appSearchQuery = it },
                                placeholder = { Text("Search apps to block...") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = SophisticatedTextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("app_search_field"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SophisticatedPrimary,
                                    unfocusedBorderColor = SophisticatedOutline,
                                    focusedTextColor = SophisticatedTextPrimary,
                                    unfocusedTextColor = SophisticatedTextPrimary
                                )
                            )

                            // App list container
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = SophisticatedCardContainer),
                                border = BorderStroke(1.dp, SophisticatedOutline),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp)
                            ) {
                                if (filteredApps.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (appSearchQuery.isEmpty()) "No apps found" else "No matching apps",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SophisticatedTextMuted
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(4.dp)
                                    ) {
                                        items(filteredApps, key = { it.packageName }) { app ->
                                            val isBlocked = selectedPackages.contains(app.packageName)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        selectedPackages = if (isBlocked) {
                                                            selectedPackages - app.packageName
                                                        } else {
                                                            selectedPackages + app.packageName
                                                        }
                                                    }
                                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Checkbox(
                                                    checked = isBlocked,
                                                    onCheckedChange = { checked ->
                                                        selectedPackages = if (checked) {
                                                            selectedPackages + app.packageName
                                                        } else {
                                                            selectedPackages - app.packageName
                                                        }
                                                    },
                                                    colors = CheckboxDefaults.colors(
                                                        checkedColor = SophisticatedPrimary,
                                                        checkmarkColor = MaterialTheme.colorScheme.onPrimary
                                                    )
                                                )

                                                if (app.icon != null) {
                                                    Image(
                                                        bitmap = app.icon.toBitmap(48, 48).asImageBitmap(),
                                                        contentDescription = app.appName,
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .clip(RoundedCornerShape(6.dp))
                                                    )
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(SophisticatedOutline),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = app.appName.take(1).uppercase(),
                                                            color = SophisticatedTextPrimary,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp
                                                        )
                                                    }
                                                }

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = app.appName,
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                        color = SophisticatedTextPrimary,
                                                        maxLines = 1
                                                    )
                                                    Text(
                                                        text = app.packageName,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = SophisticatedTextMuted,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("cancel_schedule_button"),
                        border = BorderStroke(1.dp, SophisticatedOutline)
                    ) {
                        Text("Cancel", color = SophisticatedTextSecondary)
                    }

                    Button(
                        onClick = {
                            val entity = SleepScheduleEntity(
                                id = initialSchedule?.id ?: 0L,
                                title = title.trim().ifEmpty { "Night Sleep" },
                                startHour = startHour,
                                startMinute = startMinute,
                                endHour = endHour,
                                endMinute = endMinute,
                                daysOfWeek = selectedDays.joinToString(","),
                                isEnabled = initialSchedule?.isEnabled ?: true,
                                blockedPackages = selectedPackages.joinToString(","),
                                createdAt = initialSchedule?.createdAt ?: System.currentTimeMillis()
                            )
                            onSave(entity)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("save_schedule_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SophisticatedPrimary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Schedule", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TimeAdjusterCard(
    label: String,
    hour: Int,
    minute: Int,
    modifier: Modifier = Modifier,
    onTimeChange: (Int, Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedCardContainer),
        border = BorderStroke(1.dp, SophisticatedOutline),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = SophisticatedTextMuted
            )

            // Formatted 12h display
            val formatted = SleepScheduleEntity.formatTime12h(hour, minute)
            Text(
                text = formatted,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = SophisticatedPrimary
            )

            // Hour and Minute Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hour -1 / +1
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Hour", style = MaterialTheme.typography.labelSmall, color = SophisticatedTextMuted)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val newH = if (hour == 0) 23 else hour - 1
                                onTimeChange(newH, minute)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease hour",
                                tint = SophisticatedTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = "%02d".format(hour),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = SophisticatedTextPrimary
                        )
                        IconButton(
                            onClick = {
                                val newH = (hour + 1) % 24
                                onTimeChange(newH, minute)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase hour",
                                tint = SophisticatedTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Minute -15 / +15
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Min", style = MaterialTheme.typography.labelSmall, color = SophisticatedTextMuted)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val newM = if (minute < 15) 45 else minute - 15
                                onTimeChange(hour, newM)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease minute",
                                tint = SophisticatedTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = "%02d".format(minute),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = SophisticatedTextPrimary
                        )
                        IconButton(
                            onClick = {
                                val newM = (minute + 15) % 60
                                onTimeChange(hour, newM)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase minute",
                                tint = SophisticatedTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
