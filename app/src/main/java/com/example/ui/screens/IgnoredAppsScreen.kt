package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppItem
import com.example.ui.ScreenBreakViewModel
import com.example.ui.components.AppIconView
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
fun IgnoredAppsScreen(
    viewModel: ScreenBreakViewModel,
    installedApps: List<AppItem>,
    searchQuery: String,
    filterIgnoredOnly: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val totalIgnored = installedApps.count { it.isIgnored }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NothingBlack)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Information Banner
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = NothingSurface),
            border = BorderStroke(1.dp, NothingOutline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(NothingSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsOff,
                        contentDescription = null,
                        tint = NothingWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "// EXCLUSION PROTOCOL",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        ),
                        color = NothingWhite
                    )
                    Text(
                        text = "Apps marked as ignored will operate quietly without triggering break reminders.",
                        style = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                        color = NothingTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = {
                Text(
                    "SEARCH PACKAGES...",
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = NothingTextSecondary
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search icon",
                    tint = NothingTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = NothingTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = NothingWhite,
                unfocusedTextColor = NothingWhite,
                focusedContainerColor = NothingSurface,
                unfocusedContainerColor = NothingSurface,
                focusedBorderColor = NothingRed,
                unfocusedBorderColor = NothingOutline
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("apps_search_field")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Section Header with Counts and Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !filterIgnoredOnly,
                    onClick = { viewModel.setFilterIgnoredOnly(false) },
                    label = {
                        Text(
                            "ALL [${installedApps.size}]",
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = if (!filterIgnoredOnly) FontWeight.Bold else FontWeight.Normal
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
                        selected = !filterIgnoredOnly
                    )
                )
                FilterChip(
                    selected = filterIgnoredOnly,
                    onClick = { viewModel.setFilterIgnoredOnly(true) },
                    label = {
                        Text(
                            "EXCLUDED [${totalIgnored}]",
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = if (filterIgnoredOnly) FontWeight.Bold else FontWeight.Normal
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
                        selected = filterIgnoredOnly
                    )
                )
            }

            IconButton(
                onClick = { viewModel.loadInstalledApps() },
                modifier = Modifier
                    .testTag("reload_apps_button")
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(NothingSurfaceElevated)
                    .border(BorderStroke(1.dp, NothingOutline), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reload apps",
                    tint = NothingWhite,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Content List
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = NothingRed
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "ENUMERATING PACKAGES...",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        ),
                        color = NothingTextSecondary
                    )
                }
            }
        } else if (installedApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = NothingTextMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "NO MATCHES FOR \"$searchQuery\"" else "NO PACKAGES FOUND",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = NothingTextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(
                    items = installedApps,
                    key = { it.packageName }
                ) { app ->
                    AppItemRow(
                        app = app,
                        onToggle = { viewModel.toggleAppIgnored(app) }
                    )
                }
            }
        }
    }
}

@Composable
fun AppItemRow(
    app: AppItem,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = NothingSurface),
        border = BorderStroke(
            1.dp,
            if (app.isIgnored) NothingOutlineVariant else NothingOutline
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .testTag("app_item_${app.packageName}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppIconView(
                drawable = app.icon,
                size = 40.dp
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = app.appName,
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = NothingWhite
                    )
                    if (app.isIgnored) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = NothingRed
                        ) {
                            Text(
                                text = "EXCLUDED",
                                style = androidx.compose.ui.text.TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = NothingWhite,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Text(
                    text = app.packageName,
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    ),
                    color = NothingTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Switch(
                checked = app.isIgnored,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NothingWhite,
                    checkedTrackColor = NothingRed,
                    uncheckedThumbColor = NothingTextSecondary,
                    uncheckedTrackColor = NothingOutline
                ),
                modifier = Modifier.testTag("toggle_${app.packageName}")
            )
        }
    }
}
