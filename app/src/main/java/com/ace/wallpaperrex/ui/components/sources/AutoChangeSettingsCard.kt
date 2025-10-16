package com.ace.wallpaperrex.ui.components.sources

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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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

    val autoChangeWallpaperPeriodAvailableList = listOf(15, 30, 45, 60)
    val isCustomMode = source != AutoChangeWallpaperSetting.Source.FAVORITES

    Column(modifier = modifier.fillMaxWidth()) {
        // Main toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .clickable { onEnabledChange(!enabled) }
                .padding(vertical = 12.dp),
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

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Interval selector
        IntervalSelectorSection(
            selectedInterval = intervalMinutes,
            availableIntervals = autoChangeWallpaperPeriodAvailableList,
            onIntervalSelect = onIntervalChange,
            enabled = enabled
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Source selector
        SourceSelectorSection(
            source = source,
            customSources = customSources,
            availableSources = availableSources,
            onSourceChange = onSourceChange,
            onCustomSourcesChange = onCustomSourcesChange,
            isCustomMode = isCustomMode,
            enabled = enabled
        )
    }
}

@Composable
private fun IntervalSelectorSection(
    selectedInterval: Int,
    availableIntervals: List<Int>,
    onIntervalSelect: (Int) -> Unit,
    enabled: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.38f)
    ) {
        Text(
            text = "Change Interval",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableIntervals.forEach { interval ->
                FilterChip(
                    selected = interval == selectedInterval,
                    onClick = { if (enabled) onIntervalSelect(interval) },
                    label = { Text("$interval min") },
                    enabled = enabled
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
    onCustomSourcesChange: (List<WallpaperSourceConfigItem>) -> Unit,
    isCustomMode: Boolean,
    enabled: Boolean
) {
    var localCustomMode by remember(source) { mutableStateOf(isCustomMode) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.38f)
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
                .clickable(enabled = enabled) {
                    if (localCustomMode) {
                        onSourceChange(AutoChangeWallpaperSetting.Source.FAVORITES)
                    } else {
                        onSourceChange(AutoChangeWallpaperSetting.Source.CUSTOM_SOURCES)
                    }
                    localCustomMode = !localCustomMode
                }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (localCustomMode) "Custom Sources" else "Favorites",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = if (localCustomMode) {
                        if (customSources.isEmpty())
                            "Select sources below or favorites will be used"
                        else
                            "${customSources.size} source${if (customSources.size > 1) "s" else ""} selected"
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
                modifier = Modifier.rotate(if (localCustomMode) 180f else 0f),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Custom source picker (always visible, just disabled state changes)
        if (localCustomMode) {
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
                                if (enabled) {
                                    val newSelection = if (isSelected) {
                                        customSources - sourceItem
                                    } else {
                                        customSources + sourceItem
                                    }
                                    onCustomSourcesChange(newSelection)
                                }
                            },
                            label = { Text(text = sourceItem.label) },
                            enabled = enabled
                        )
                    }
                }

                if (customSources.isEmpty()) {
                    Text(
                        text = "No sources selected. Favorites will be used as fallback.",
                        color = if (enabled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}