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
import com.ace.wallpaperrex.data.daos.UserPrefsKeys
import com.ace.wallpaperrex.ui.components.sources.SourceSettingCard
import com.ace.wallpaperrex.ui.components.sources.WallhavenSetting
import com.ace.wallpaperrex.ui.models.WallpaperSourceItem
import com.ace.wallpaperrex.ui.models.wallpaperSourcesStatic
import com.ace.wallpaperrex.ui.screens.wallpapers.WallpaperSourceViewModel
import com.ace.wallpaperrex.ui.theme.AppTheme


@Composable()
fun SettingsScreen(
    modifier: Modifier = Modifier,
    wallpaperSourceViewModel: WallpaperSourceViewModel = viewModel()
) {
    val wallpaperSources by wallpaperSourceViewModel.sources.collectAsState()

    if (wallpaperSources.isEmpty()) {
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
                    text = "Sources Configurations",
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            // List items
            itemsIndexed( // Using itemsIndexed if you use index as key, or items with key lambda
                items = wallpaperSourcesStatic,
                key = { _, source -> source.id } // Use source.id for stable keys
            ) { index, sourceItem ->
                val sourceModel = wallpaperSources.find { it.id == sourceItem.id };
                WallpaperSourceRow(
                    source = sourceModel!!,
                    onApiKeySave = { id, apiKey ->
                        wallpaperSourceViewModel.updateWallpaperApiKey(
                            apiKeyDatastoreKey = sourceItem.apiKeyDataStoreKey,
                            apiKey = apiKey
                        )
                    },
                    onSetAsDefault = {
                        wallpaperSourceViewModel.setAsDefault(sourceItem.id)
                    }
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
            source = WallpaperSourceItem(
                1, "Unsplash", "Photos",
                apiKey = null,
                apiKeyDataStoreKey = UserPrefsKeys.UNSPLASH_API_KEY,
                isDefault = false,
            ),
            onApiKeySave = { _, _ -> },
            onSetAsDefault = { _ -> },
        )
    }
}

@Composable
fun WallpaperSourceRow(
    source: WallpaperSourceItem,
    onApiKeySave: (sourceId: Int, apiKey: String) -> Unit,
    modifier: Modifier = Modifier,
    onSetAsDefault: (source: WallpaperSourceItem) -> Unit
) {
    SourceSettingCard(
        source = source,
        modifier = modifier,
        onSetAsDefault = {
            onSetAsDefault(source)
        }
    ) {
        when (source.id) {
            1, 2 -> {
                WallhavenSetting(source = source, onApiKeySave = onApiKeySave)
            }

            else -> {
                Text(
                    "No configuration needed/supported currently",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}