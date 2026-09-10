package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.FilterList
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppItem
import com.example.ui.ScreenBreakViewModel
import com.example.ui.components.AppIconView
import com.example.ui.theme.AppleSystemGreen
import com.example.ui.theme.SophisticatedCardContainer
import com.example.ui.theme.SophisticatedOnPrimary
import com.example.ui.theme.SophisticatedOutline
import com.example.ui.theme.SophisticatedPrimary
import com.example.ui.theme.SophisticatedSurface
import com.example.ui.theme.SophisticatedSurfaceVariant
import com.example.ui.theme.SophisticatedTextMuted
import com.example.ui.theme.SophisticatedTextPrimary
import com.example.ui.theme.SophisticatedTextSecondary

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
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Information banner
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
            border = BorderStroke(1.dp, SophisticatedOutline),
            modifier = Modifier.fillMaxWidth()
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
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SophisticatedCardContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsOff,
                        contentDescription = null,
                        tint = SophisticatedPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Apps that won't trigger pop-ups",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = SophisticatedTextPrimary
                    )
                    Text(
                        text = "When using an ignored app, screen break pop-ups stay suppressed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SophisticatedTextSecondary
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
                    "Search apps...",
                    color = SophisticatedTextSecondary
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search icon",
                    tint = SophisticatedTextSecondary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = SophisticatedTextSecondary
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = SophisticatedTextPrimary,
                unfocusedTextColor = SophisticatedTextPrimary,
                focusedContainerColor = SophisticatedSurfaceVariant,
                unfocusedContainerColor = SophisticatedSurfaceVariant,
                focusedBorderColor = SophisticatedPrimary,
                unfocusedBorderColor = SophisticatedOutline
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("apps_search_field")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Section header with title & counts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "IGNORE THESE APPS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = SophisticatedPrimary
            )
            Text(
                text = "$totalIgnored Selected",
                style = MaterialTheme.typography.labelSmall,
                color = SophisticatedTextMuted
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filter chips and reload
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !filterIgnoredOnly,
                    onClick = { viewModel.setFilterIgnoredOnly(false) },
                    label = { Text("All (${installedApps.size})") },
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
                        selected = !filterIgnoredOnly
                    )
                )
                FilterChip(
                    selected = filterIgnoredOnly,
                    onClick = { viewModel.setFilterIgnoredOnly(true) },
                    label = { Text("Ignored ($totalIgnored)") },
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
                        selected = filterIgnoredOnly
                    )
                )
            }

            IconButton(
                onClick = { viewModel.loadInstalledApps() },
                modifier = Modifier
                    .testTag("reload_apps_button")
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SophisticatedSurfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reload apps",
                    tint = SophisticatedTextSecondary,
                    modifier = Modifier.size(18.dp)
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
                        modifier = Modifier.size(40.dp),
                        color = SophisticatedPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading installed apps...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SophisticatedTextSecondary
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
                        modifier = Modifier.size(56.dp),
                        tint = SophisticatedOutline
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No apps found matching \"$searchQuery\"" else "No apps available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SophisticatedTextSecondary
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
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = SophisticatedSurfaceVariant
        ),
        border = BorderStroke(
            1.dp,
            if (app.isIgnored) SophisticatedPrimary.copy(alpha = 0.5f) else SophisticatedOutline
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .testTag("app_item_${app.packageName}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AppIconView(
                drawable = app.icon,
                size = 44.dp
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = SophisticatedTextPrimary
                    )
                    if (app.isIgnored) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SophisticatedPrimary
                        ) {
                            Text(
                                text = "IGNORED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = SophisticatedOnPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = SophisticatedTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Switch(
                checked = app.isIgnored,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AppleSystemGreen,
                    uncheckedThumbColor = SophisticatedTextSecondary,
                    uncheckedTrackColor = SophisticatedOutline
                ),
                modifier = Modifier.testTag("toggle_${app.packageName}")
            )
        }
    }
}
