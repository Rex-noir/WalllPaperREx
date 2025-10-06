package com.ace.wallpaperrex.ui.screens.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ace.wallpaperrex.ui.models.WallpaperSourceItem
import com.ace.wallpaperrex.ui.components.sources.SourceSettingCard
import com.ace.wallpaperrex.ui.components.sources.WallhavenSetting
import com.ace.wallpaperrex.ui.theme.AppTheme


@Composable()
fun SettingsScreen(
    modifier: Modifier = Modifier, // This modifier will be passed to LazyColumn
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val wallpaperSources by settingsViewModel.wallpaperSourcesState.collectAsState()

    if (wallpaperSources.isEmpty()) {
        // Handle empty state - you might want a Column here for centering, or just Text
        Column(
            modifier = modifier // Apply the main modifier here for consistent padding/sizing
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally, // Example alignment
            verticalArrangement = Arrangement.Center
        ) {
            Text("No Wallpaper sources available")
        }
    } else {
        LazyColumn(
            modifier = modifier, // Apply the passed-in modifier directly here
            contentPadding = PaddingValues(
                horizontal = 16.dp,
                vertical = 8.dp
            ), // Adjust padding as needed
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header item for the title
            item {
                Text(
                    text = "Sources",
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            // List items
            itemsIndexed( // Using itemsIndexed if you use index as key, or items with key lambda
                items = wallpaperSources,
                key = { _, source -> source.id } // Use source.id for stable keys
            ) { index, sourceItem ->
                WallpaperSourceRow(
                    source = sourceItem,
                    onToggleEnabled = { settingsViewModel.toggleSourceEnabled(sourceItem.id) }, // Pass sourceItem.id
                    onApiKeySave = { id, apiKey ->
                        settingsViewModel.saveApiKey(
                            id,
                            apiKey
                        )
                    } // Assuming saveApiKey exists
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    AppTheme {
        // For preview, you might need to provide a fake ViewModel
        // or ensure your default wallpaperSources list is non-empty
        // and doesn't rely on Android-specific context for initialization.
        // If SettingsViewModel has no Android dependencies in its constructor
        // or init, this should work.
        SettingsScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun WallpaperSourceRowPreview() {
    AppTheme {
        WallpaperSourceRow(
            source = WallpaperSourceItem(1, "Unsplash", "Photos", true),
            onApiKeySave = { _, _ -> },
            onToggleEnabled = {})
    }
}

@Composable
fun WallpaperSourceRow(
    source: WallpaperSourceItem,
    onToggleEnabled: (id: Int) -> Unit,
    onApiKeySave: (sourceId: Int, apiKey: String) -> Unit,
    modifier: Modifier = Modifier
) {
    SourceSettingCard(
        source = source,
        onToggleEnabled = onToggleEnabled,
        modifier = modifier
    ) {
        when (source.name) {
            "Wallhaven" -> {
                WallhavenSetting(source = source, onApiKeySave = onApiKeySave)
            }

            else -> {
//                Text("No configuration needed", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}