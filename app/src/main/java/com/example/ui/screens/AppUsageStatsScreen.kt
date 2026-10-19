package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ScreenBreakViewModel
import com.example.ui.components.AppIconView
import com.example.ui.theme.NothingBlack
import com.example.ui.theme.NothingOutline
import com.example.ui.theme.NothingRed
import com.example.ui.theme.NothingSurface
import com.example.ui.theme.NothingTextMuted
import com.example.ui.theme.NothingTextSecondary
import com.example.ui.theme.NothingWhite
import com.example.util.AppUsageItem
import com.example.util.DeviceUsageStatsHelper
import com.example.util.ReelsTrackingManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AppUsageStatsScreen(
    viewModel: ScreenBreakViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val liveReelsToday by ReelsTrackingManager.todayReelsFlow.collectAsState()
    var appUsageList by remember { mutableStateOf<List<AppUsageItem>>(emptyList()) }
    var todayTotalMinutes by remember { mutableIntStateOf(0) }
    var changePercentage by remember { mutableStateOf(0.0) }
    var reelsToday by remember { mutableIntStateOf(ReelsTrackingManager.getTodayCount(context)) }
    var reelsChangePercent by remember { mutableStateOf(0.0) }
    var isReelsBlocking by remember { mutableStateOf(ReelsTrackingManager.isReelsBlockingEnabled(context)) }
    var isCenterHUDOn by remember { mutableStateOf(ReelsTrackingManager.isCenterTimerOverlayEnabled(context)) }
    var weeklyReels by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(liveReelsToday) {
        reelsToday = liveReelsToday
    }

    fun refreshStats() {
        isLoading = true
        coroutineScope.launch {
            val list = withContext(Dispatchers.IO) {
                DeviceUsageStatsHelper.getTodayAppUsageList(context)
            }
            val calculatedTotal = withContext(Dispatchers.IO) {
                DeviceUsageStatsHelper.getTodayTotalMinutes(context)
            }
            val totalMins = maxOf(calculatedTotal, list.sumOf { it.usageMinutes })
            val change = withContext(Dispatchers.IO) {
                DeviceUsageStatsHelper.getScreenTimeChangePercentage(context)
            }
            val reelsCount = ReelsTrackingManager.getTodayCount(context)
            val reelsChange = ReelsTrackingManager.getChangePercentage(context)
            val weekly = ReelsTrackingManager.getLast7DaysReels(context)

            appUsageList = list
            todayTotalMinutes = totalMins
            changePercentage = change
            reelsToday = reelsCount
            reelsChangePercent = reelsChange
            weeklyReels = weekly
            isLoading = false
        }
    }

    LifecycleResumeEffect(Unit) {
        refreshStats()
        onPauseOrDispose { }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NothingBlack)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
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
                    text = "USAGE PROTOCOL // METRICS",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp
                    ),
                    color = NothingRed
                )
                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = { refreshStats() },
                    modifier = Modifier.size(32.dp).testTag("refresh_stats_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh stats",
                        tint = NothingTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "USAGE STATS",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    letterSpacing = 0.5.sp
                ),
                color = NothingWhite
            )

            Text(
                text = "Telemetry & short-form video monitoring",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                ),
                color = NothingTextSecondary
            )
        }

        // Screen Time Hero Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(NothingSurface)
                    .border(BorderStroke(1.dp, NothingOutline), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "TOTAL SCREEN TIME // TODAY",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            ),
                            color = NothingTextSecondary
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        val pctText = when {
                            changePercentage > 0 -> "+%.1f%% vs yday".format(changePercentage)
                            changePercentage < 0 -> "%.1f%% vs yday".format(changePercentage)
                            else -> "0.0% vs yday"
                        }
                        Text(
                            text = pctText,
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = if (changePercentage > 0) NothingRed else Color(0xFF4CAF50)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = DeviceUsageStatsHelper.formatDuration(todayTotalMinutes),
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 38.sp,
                            letterSpacing = 1.sp
                        ),
                        color = NothingWhite
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Aggregated across ${appUsageList.size} applications today",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        color = NothingTextMuted
                    )
                }
            }
        }

        // Reels & Short-form Detox Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(NothingSurface)
                    .border(
                        BorderStroke(1.dp, if (isReelsBlocking) NothingRed else NothingOutline),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "SHORT-FORM DETOX",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            ),
                            color = if (isReelsBlocking) NothingRed else NothingWhite
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = if (isReelsBlocking) "● BLOCKED" else "● TRACKING",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = if (isReelsBlocking) NothingRed else NothingTextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "$reelsToday",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 36.sp
                            ),
                            color = NothingWhite
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "REELS SCROLLED TODAY",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            ),
                            color = NothingTextSecondary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 7-day mini bar chart
                    if (weeklyReels.isNotEmpty()) {
                        val maxCount = weeklyReels.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(vertical = 4.dp)
                        ) {
                            weeklyReels.forEach { (dayLabel, count) ->
                                val heightFrac = (count.toFloat() / maxCount.toFloat()).coerceIn(0.08f, 1f)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(14.dp)
                                            .weight(1f, fill = false)
                                            .height((36 * heightFrac).dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (dayLabel == "TODAY") NothingRed
                                                else NothingOutline
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = dayLabel.take(1),
                                        style = TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp
                                        ),
                                        color = if (dayLabel == "TODAY") NothingWhite else NothingTextMuted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Block Reels Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NothingBlack.copy(alpha = 0.5f))
                            .border(BorderStroke(0.5.dp, NothingOutline), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "BLOCK REELS & SHORTS",
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                color = NothingWhite
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Auto-blocks Reels, YT Shorts & TikTok",
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp
                                ),
                                color = NothingTextSecondary
                            )
                        }

                        Switch(
                            checked = isReelsBlocking,
                            onCheckedChange = { checked ->
                                isReelsBlocking = checked
                                ReelsTrackingManager.setReelsBlockingEnabled(context, checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NothingWhite,
                                checkedTrackColor = NothingRed,
                                uncheckedThumbColor = NothingTextMuted,
                                uncheckedTrackColor = NothingBlack,
                                uncheckedBorderColor = NothingOutline
                            ),
                            modifier = Modifier.testTag("switch_block_reels")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Center Screen HUD Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NothingBlack.copy(alpha = 0.5f))
                            .border(BorderStroke(0.5.dp, NothingOutline), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CENTER TIME HUD",
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                color = NothingWhite
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Displays live time elapsed at screen center",
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp
                                ),
                                color = NothingTextSecondary
                            )
                        }

                        Switch(
                            checked = isCenterHUDOn,
                            onCheckedChange = { checked ->
                                isCenterHUDOn = checked
                                ReelsTrackingManager.setCenterTimerOverlayEnabled(context, checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NothingWhite,
                                checkedTrackColor = NothingRed,
                                uncheckedThumbColor = NothingTextMuted,
                                uncheckedTrackColor = NothingBlack,
                                uncheckedBorderColor = NothingOutline
                            ),
                            modifier = Modifier.testTag("switch_center_hud")
                        )
                    }
                }
            }
        }

        // Section Title: App Breakdown
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = "APPLICATION BREAKDOWN",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    ),
                    color = NothingTextSecondary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${appUsageList.size} APPS",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    ),
                    color = NothingTextMuted
                )
            }
        }

        if (appUsageList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isLoading) "CALCULATING USAGE TELEMETRY..." else "NO USAGE TELEMETRY YET TODAY",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        ),
                        color = NothingTextMuted
                    )
                }
            }
        } else {
            items(appUsageList, key = { it.packageName }) { appItem ->
                AppUsageRow(
                    item = appItem,
                    onExcludeApp = {
                        coroutineScope.launch {
                            viewModel.setAppIgnored(appItem.packageName, appItem.appName, true)
                        }
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun AppUsageRow(
    item: AppUsageItem,
    onExcludeApp: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(NothingSurface)
            .border(BorderStroke(1.dp, NothingOutline), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AppIconView(
                    drawable = item.icon,
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.appName,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = NothingWhite,
                        maxLines = 1
                    )
                    Text(
                        text = item.packageName,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        color = NothingTextMuted,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = DeviceUsageStatsHelper.formatDuration(item.usageMinutes),
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = NothingWhite
                    )
                    Text(
                        text = "%.0f%%".format(item.percentageOfTotal * 100),
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        color = NothingTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { item.percentageOfTotal.coerceIn(0.02f, 1f) },
                color = NothingRed,
                trackColor = NothingBlack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
            )
        }
    }
}
