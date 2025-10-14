package com.ace.wallpaperrex.ui.screens.setting

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ace.wallpaperrex.data.repositories.WallpaperSourceRepository
import com.ace.wallpaperrex.ui.components.sources.AutoChangeSettingsCard

@Composable
fun GeneralSettingScreen(
    modifier: Modifier = Modifier,
    viewModel: GeneralSettingViewModel = viewModel(),
    sourceRepository: WallpaperSourceRepository
) {
    val state by viewModel.state.collectAsState()
    val sources by sourceRepository.wallpaperSources.collectAsState(initial = emptyList())

    LazyColumn(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        item {
            AutoChangeSettingsCard(
                enabled = state.autoChangeWallpaper,
                onEnabledChange = viewModel::setAutoChangeWallpaper,
                intervalMinutes = state.autoChangeWallpaperInterval,
                availableIntervals = GeneralSettingViewModel.autoChangeWallpaperPeriodAvailableList,
                onIntervalChange = viewModel::setAutoChangeWallpaperInterval,
                source = state.autoChangeWallpaperSource,
                onSourceChange = viewModel::setAutoChangeWallpaperSource,
                customSources = state.autoChangeCustomSources,
                availableSources = sources,
                onCustomSourcesChange = viewModel::setAutoChangeCustomSources
            )
        }
    }
}
