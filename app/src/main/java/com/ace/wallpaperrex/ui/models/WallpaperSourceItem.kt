package com.ace.wallpaperrex.ui.models

import androidx.datastore.preferences.core.Preferences
import com.ace.wallpaperrex.data.daos.UserPrefsKeys

data class WallpaperSourceItem(
    val id: Int,
    val name: String,
    val description: String,
    val apiKey: String? = "",
    val apiKeyDataStoreKey: Preferences.Key<String>
)

val wallpaperSources = listOf<WallpaperSourceItem>(
    WallpaperSourceItem(
        id = 1,
        name = "Wallhaven",
        description = "A popular source for high-quality desktop wallpapers.",
        apiKeyDataStoreKey = UserPrefsKeys.WALLHAVEN_API_KEY
    ),
    WallpaperSourceItem(
        id = 2,
        name = "Unsplash",
        description = "A collection of freely-usable high-resolution photos.",
        apiKeyDataStoreKey = UserPrefsKeys.UNSPLASH_API_KEY
    ),
)