package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import com.example.data.SleepScheduleEntity
import com.example.model.AppItem
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
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NothingSurface),
            border = BorderStroke(1.dp, NothingOutline),
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
                    Column {
                        Text(
                            text = "// SCHEDULE CONFIG",
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            ),
                            color = NothingRed
                        )
                        Text(
                            text = if (initialSchedule == null) "NEW SLEEP SCHEDULE" else "EDIT SCHEDULE",
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = NothingWhite
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_editor_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = NothingTextSecondary
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
                                text = "SCHEDULE NAME",
                                style = androidx.compose.ui.text.TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = NothingTextSecondary
                            )
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                placeholder = {
                                    Text(
                                        "e.g. Night Sleep, Bedtime Wind-down",
                                        color = NothingTextMuted
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("schedule_title_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NothingRed,
                                    unfocusedBorderColor = NothingOutline,
                                    focusedTextColor = NothingWhite,
                                    unfocusedTextColor = NothingWhite,
                                    focusedContainerColor = NothingSurfaceElevated,
                                    unfocusedContainerColor = NothingSurfaceElevated
                                )
                            )
                        }
                    }

                    // Time Window Pickers
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "RESTRICTION WINDOW",
                                style = androidx.compose.ui.text.TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = NothingTextSecondary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Start Time
                                TimeAdjusterCard(
                                    label = "START [BEDTIME]",
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
                                    label = "END [WAKE UP]",
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
                                    text = "ACTIVE DAYS",
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = NothingTextSecondary
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "ALL",
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = NothingRed,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable {
                                                selectedDays = SleepScheduleEntity.ALL_DAYS.toSet()
                                            }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                    Text(
                                        text = "WEEKDAYS",
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = NothingRed,
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
                                        label = {
                                            Text(
                                                day,
                                                style = androidx.compose.ui.text.TextStyle(
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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
                                        tint = NothingRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "BLOCKED APPS [${selectedPackages.size}]",
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = NothingWhite
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "SELECT ALL",
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = NothingRed,
                                        modifier = Modifier
                                            .clickable {
                                                selectedPackages = installedApps.map { it.packageName }.toSet()
                                            }
                                            .padding(4.dp)
                                    )
                                    Text(
                                        text = "CLEAR",
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = NothingTextMuted,
                                        modifier = Modifier
                                            .clickable {
                                                selectedPackages = emptySet()
                                            }
                                            .padding(4.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Selected apps will be blocked from opening during schedule window.",
                                style = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                                color = NothingTextSecondary
                            )

                            // Search Field
                            OutlinedTextField(
                                value = appSearchQuery,
                                onValueChange = { appSearchQuery = it },
                                placeholder = {
                                    Text(
                                        "SEARCH APPS TO RESTRICT...",
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp
                                        ),
                                        color = NothingTextMuted
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = NothingTextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("app_search_field"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NothingRed,
                                    unfocusedBorderColor = NothingOutline,
                                    focusedTextColor = NothingWhite,
                                    unfocusedTextColor = NothingWhite,
                                    focusedContainerColor = NothingSurfaceElevated,
                                    unfocusedContainerColor = NothingSurfaceElevated
                                )
                            )

                            // App list container
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = NothingBlack),
                                border = BorderStroke(1.dp, NothingOutline),
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
                                            text = if (appSearchQuery.isEmpty()) "NO APPS FOUND" else "NO MATCHING APPS",
                                            style = androidx.compose.ui.text.TextStyle(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp
                                            ),
                                            color = NothingTextMuted
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
                                                        checkedColor = NothingRed,
                                                        checkmarkColor = NothingWhite,
                                                        uncheckedColor = NothingOutline
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
                                                            .background(NothingSurfaceElevated),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = app.appName.take(1).uppercase(),
                                                            color = NothingWhite,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp
                                                        )
                                                    }
                                                }

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = app.appName,
                                                        style = androidx.compose.ui.text.TextStyle(
                                                            fontFamily = FontFamily.SansSerif,
                                                            fontWeight = FontWeight.Medium,
                                                            fontSize = 13.sp
                                                        ),
                                                        color = NothingWhite,
                                                        maxLines = 1
                                                    )
                                                    Text(
                                                        text = app.packageName,
                                                        style = androidx.compose.ui.text.TextStyle(
                                                            fontFamily = FontFamily.Monospace,
                                                            fontSize = 10.sp
                                                        ),
                                                        color = NothingTextMuted,
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
                            .height(46.dp)
                            .testTag("cancel_schedule_button"),
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, NothingOutline)
                    ) {
                        Text(
                            "CANCEL",
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            ),
                            color = NothingTextSecondary
                        )
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
                            .height(46.dp)
                            .testTag("save_schedule_button"),
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NothingRed,
                            contentColor = NothingWhite
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "SAVE SCHEDULE",
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

@Composable
fun TimeAdjusterCard(
    label: String,
    hour: Int,
    minute: Int,
    modifier: Modifier = Modifier,
    onTimeChange: (Int, Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = NothingSurfaceElevated),
        border = BorderStroke(1.dp, NothingOutline),
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
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                ),
                color = NothingTextMuted
            )

            // Formatted 12h display
            val formatted = SleepScheduleEntity.formatTime12h(hour, minute)
            Text(
                text = formatted,
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = 0.5.sp
                ),
                color = NothingWhite
            )

            // Hour and Minute Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hour -1 / +1
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "HR",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp
                        ),
                        color = NothingTextMuted
                    )
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
                                tint = NothingTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = "%02d".format(hour),
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = NothingWhite
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
                                tint = NothingTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Minute -15 / +15
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "MIN",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp
                        ),
                        color = NothingTextMuted
                    )
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
                                tint = NothingTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = "%02d".format(minute),
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = NothingWhite
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
                                tint = NothingTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
