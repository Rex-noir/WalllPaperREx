package com.ace.wallpaperrex.ui.screens.setting

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ace.wallpaperrex.ui.components.sources.AutoChangeSettingsCard
import com.ace.wallpaperrex.ui.screens.models.AutoChangeWallpaperSetting

@Composable
fun GeneralSettingScreen(
    modifier: Modifier = Modifier,
    viewModel: GeneralSettingViewModel,
) {
    val sources by viewModel.wallpaperSources.collectAsState(initial = emptyList())
    val autoChangeSettings by viewModel.autoChangeSetting.collectAsState(initial = AutoChangeWallpaperSetting())

    LazyColumn(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        item {
            AutoChangeSettingsCard(
                autoChangeWallpaperSetting = autoChangeSettings,
                modifier = Modifier.padding(vertical = 8.dp),
                onIntervalChange = viewModel::setAutoChangeWallpaperInterval,
                onSourceChange = viewModel::setAutoChangeWallpaperSource,
                onCustomSourcesChange = viewModel::setAutoChangeCustomSources,
                onEnabledChange = viewModel::updateAutoChangeWallpaperEnabled,
                availableSources = sources
            )
        }
    }
}
