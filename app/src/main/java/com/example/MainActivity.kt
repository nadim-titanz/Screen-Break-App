package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ScreenBreakViewModel
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.IgnoredAppsScreen
import com.example.ui.screens.SettingsDialog
import com.example.ui.screens.SleepSchedulesScreen
import com.example.ui.theme.BrickAccentDot
import com.example.ui.theme.BrickBackground
import com.example.ui.theme.BrickOutline
import com.example.ui.theme.BrickSurface
import com.example.ui.theme.BrickTextMuted
import com.example.ui.theme.BrickTextPrimary
import com.example.ui.theme.BrickTextSecondary
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ScreenBreakViewModel by viewModels {
        val app = application as ScreenBreakApplication
        ScreenBreakViewModel.Factory(app.repository, applicationContext)
    }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            // Handled
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            MyApplicationTheme {
                ScreenBreakApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermissions()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenBreakApp(viewModel: ScreenBreakViewModel) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }

    val usageState by viewModel.usageState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterIgnoredOnly by viewModel.filterIgnoredOnly.collectAsStateWithLifecycle()
    val isLoadingApps by viewModel.isLoadingApps.collectAsStateWithLifecycle()
    val recentLogs by viewModel.recentLogs.collectAsStateWithLifecycle()
    val reminderCount by viewModel.reminderCount.collectAsStateWithLifecycle()
    val suppressedCount by viewModel.suppressedCount.collectAsStateWithLifecycle()
    val hasUsageAccess by viewModel.hasUsageAccess.collectAsStateWithLifecycle()
    val hasOverlayPermission by viewModel.hasOverlayPermission.collectAsStateWithLifecycle()

    val ignoredCount = installedApps.count { it.isIgnored }

    Scaffold(
        containerColor = BrickBackground,
        topBar = {
            // Minimal Header inspired by Brick
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_icon),
                        contentDescription = "Interrupt logo",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = when (selectedTab) {
                                0 -> "Interrupt"
                                1 -> "Schedule"
                                2 -> "Settings"
                                else -> "Interrupt"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 17.sp,
                                letterSpacing = (-0.3).sp
                            ),
                            color = BrickTextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (usageState.isServiceRunning) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(BrickAccentDot)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = if (usageState.isServiceRunning) "Focus active" else "Paused",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = BrickTextSecondary
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { showSettingsDialog = true },
                    modifier = Modifier
                        .testTag("settings_button")
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BrickSurface)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = BrickTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        bottomBar = {
            BrickBottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                ignoredCount = ignoredCount,
                modifier = Modifier.testTag("main_navigation_bar")
            )
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "tab_transition",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { tab ->
            when (tab) {
                0 -> HomeScreen(
                    viewModel = viewModel,
                    usageState = usageState,
                    reminderCount = reminderCount,
                    suppressedCount = suppressedCount,
                    ignoredAppsCount = ignoredCount,
                    hasUsageAccess = hasUsageAccess,
                    hasOverlayPermission = hasOverlayPermission,
                    onNavigateToIgnored = { selectedTab = 2 }
                )
                1 -> SleepSchedulesScreen(
                    viewModel = viewModel
                )
                2 -> IgnoredAppsScreen(
                    viewModel = viewModel,
                    installedApps = installedApps,
                    searchQuery = searchQuery,
                    filterIgnoredOnly = filterIgnoredOnly,
                    isLoading = isLoadingApps
                )
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            viewModel = viewModel,
            settings = settings,
            onDismiss = { showSettingsDialog = false }
        )
    }
}

/**
 * Clean, text-first bottom navigation bar directly inspired by the Brick app screenshot:
 * Shows 4 tabs ("Break", "Schedule", "Activity", "Settings").
 * Active tab has white text and a small centered dot directly beneath.
 */
@Composable
fun BrickBottomNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    ignoredCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        color = BrickBackground,
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                drawLine(
                    color = BrickOutline,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = strokeWidth
                )
            }
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabLabels = listOf("Interrupt", "Schedule", "Settings")
            val tabTags = listOf("tab_dashboard", "tab_schedule", "tab_ignored_apps")

            tabLabels.forEachIndexed { index, label ->
                val isSelected = selectedTab == index
                val interactionSource = remember { MutableInteractionSource() }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple(bounded = false, radius = 32.dp),
                            onClick = { onTabSelected(index) }
                        )
                        .padding(vertical = 6.dp)
                        .testTag(tabTags[index]),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 15.sp,
                            letterSpacing = (-0.2).sp
                        ),
                        color = if (isSelected) BrickTextPrimary else BrickTextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(BrickTextPrimary)
                        )
                    } else {
                        Spacer(modifier = Modifier.size(4.dp))
                    }
                }
            }
        }
    }
}
