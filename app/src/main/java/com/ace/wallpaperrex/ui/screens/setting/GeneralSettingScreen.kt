package com.ace.wallpaperrex.ui.screens.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun GeneralSettingScreen(
    modifier: Modifier = Modifier,
    viewModel: GeneralSettingViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LazyColumn(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        item {
            AutoChangeSetting(
                autoChangeWallpaper = state.autoChangeWallpaper,
                onAutoChangeWallpaperCheckChange = viewModel::setAutoChangeWallpaper,
                autoChangeIntervalMinutesList = GeneralSettingViewModel.autoChangeWallpaperPeriodAvailableList,
                onAutoChangeIntervalMinutesCheckChange = viewModel::setAutoChangeWallpaperInterval,
                autoChangeIntervalMinutes = state.autoChangeWallpaperInterval
            )
        }
    }
}

@Composable
fun AutoChangeSetting(
    modifier: Modifier = Modifier,
    autoChangeWallpaper: Boolean,
    onAutoChangeWallpaperCheckChange: (Boolean) -> Unit,
    autoChangeIntervalMinutesList: List<Int>,
    onAutoChangeIntervalMinutesCheckChange: (Int) -> Unit,
    autoChangeIntervalMinutes: Int
) {
    var expanded by remember { mutableStateOf(false) }

    var cardWidth: IntSize by remember { mutableStateOf(IntSize.Zero) }
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SettingSwitch(
            title = "Enable auto change wallpaper",
            subtitle = "Periodically changes your device's wallpaper.",
            checked = autoChangeWallpaper,
            onCheckedChange = onAutoChangeWallpaperCheckChange
        )

        if (autoChangeWallpaper) {
            Box {
                // Card that acts as the select button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged({ cardWidth = it })
                        .clickable { expanded = true },
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Interval ($autoChangeIntervalMinutes minutes)",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "How often to change the wallpaper.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .width(with(LocalDensity.current) { cardWidth.width.toDp() }) // match card width
                        .padding(16.dp)
                ) {
                    autoChangeIntervalMinutesList.forEach { interval ->
                        DropdownMenuItem(
                            text = { Text("$interval minutes") },
                            onClick = {
                                onAutoChangeIntervalMinutesCheckChange(interval)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingSwitch(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .clickable { onCheckedChange(!checked) }
                .padding(all = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = null // Click is handled by the parent Row
            )
        }
    }
}