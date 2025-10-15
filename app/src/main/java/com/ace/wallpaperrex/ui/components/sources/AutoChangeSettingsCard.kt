package com.ace.wallpaperrex.ui.components.sources

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.ace.wallpaperrex.data.models.WallpaperSourceConfigItem
import com.ace.wallpaperrex.ui.screens.models.AutoChangeWallpaperSetting

@Composable
fun AutoChangeSettingsCard(
    autoChangeWallpaperSetting: AutoChangeWallpaperSetting,
    modifier: Modifier = Modifier,
    onIntervalChange: (Int) -> Unit,
    onSourceChange: (AutoChangeWallpaperSetting.Source) -> Unit,
    onCustomSourcesChange: (List<WallpaperSourceConfigItem>) -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    availableSources: List<WallpaperSourceConfigItem>,
) {
    val intervalMinutes = autoChangeWallpaperSetting.interval
    val enabled = autoChangeWallpaperSetting.enabled
    val source = autoChangeWallpaperSetting.source
    val customSources = autoChangeWallpaperSetting.customSources


    val autoChangeWallpaperPeriodAvailableList =
        listOf(15, 30, 45, 60)

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Main toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .clickable { onEnabledChange(!enabled) }
                    .padding(all = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Auto Change Wallpaper",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Periodically changes your device's wallpaper",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = null
                )
            }

            // Settings content (visible when enabled)
            AnimatedVisibility(visible = enabled) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    // Interval selector
                    IntervalSelectorSection(
                        selectedInterval = intervalMinutes,
                        availableIntervals = autoChangeWallpaperPeriodAvailableList,
                        onIntervalSelect = onIntervalChange
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Source selector
                    SourceSelectorSection(
                        source = source,
                        customSources = customSources,
                        availableSources = availableSources,
                        onSourceChange = onSourceChange,
                        onCustomSourcesChange = onCustomSourcesChange
                    )
                }
            }
        }
    }
}

@Composable
private fun IntervalSelectorSection(
    selectedInterval: Int,
    availableIntervals: List<Int>,
    onIntervalSelect: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Text(
            text = "Change Interval",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Interval chips
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableIntervals.forEach { interval ->
                FilterChip(
                    selected = interval == selectedInterval,
                    onClick = { onIntervalSelect(interval) },
                    label = { Text("$interval min") }
                )
            }
        }

        Text(
            text = "Wallpaper will change every $selectedInterval minutes",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun SourceSelectorSection(
    source: AutoChangeWallpaperSetting.Source,
    customSources: List<WallpaperSourceConfigItem>,
    availableSources: List<WallpaperSourceConfigItem>,
    onSourceChange: (AutoChangeWallpaperSetting.Source) -> Unit,
    onCustomSourcesChange: (List<WallpaperSourceConfigItem>) -> Unit
) {
    val isCustomMode =
        source != AutoChangeWallpaperSetting.Source.FAVORITES
    var showSourcePicker by remember { mutableStateOf(false) }

    LaunchedEffect(customSources, isCustomMode) {
        showSourcePicker = customSources.isNotEmpty() && isCustomMode
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Text(
            text = "Wallpaper Source",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Source mode selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .clickable {
                    val newMode = !isCustomMode
                    onSourceChange(
                        if (newMode) {
                            AutoChangeWallpaperSetting.Source.CUSTOM_SOURCES
                        } else {
                            AutoChangeWallpaperSetting.Source.FAVORITES
                        }
                    )
                    showSourcePicker = newMode
                }
                .padding(all = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isCustomMode) "Custom Sources" else "Favorites",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = if (isCustomMode) {
                        if (customSources.isEmpty())
                            "Please select at least one source or wallpapers from favorites will be used automatically"
                        else
                            "${customSources.size} sources selected."
                    } else {
                        "Use wallpapers from your favorites"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Toggle sources",
                modifier = Modifier.rotate(if (showSourcePicker) 180f else 0f),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Custom source picker
        AnimatedVisibility(visible = isCustomMode && showSourcePicker) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "Select Sources",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableSources.forEach { sourceItem ->
                        val isSelected = customSources.contains(sourceItem)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val newSelection = if (isSelected) {
                                    customSources - sourceItem
                                } else {
                                    customSources + sourceItem
                                }
                                onCustomSourcesChange(newSelection)
                            },
                            label = { Text(text = sourceItem.label) }
                        )
                    }
                    if (customSources.isEmpty()) {
                        Text(
                            "Please select at least one source.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}