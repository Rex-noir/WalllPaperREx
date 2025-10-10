package com.ace.wallpaperrex.ui.models

import androidx.datastore.preferences.core.Preferences
import com.ace.wallpaperrex.data.daos.UserPrefsKeys

data class WallpaperSourceItem(
    val id: Int,
    val name: String,
    val description: String,
    val apiKey: String? = "",
    val apiKeyDataStoreKey: Preferences.Key<String>,
    val isDefault: Boolean = false,
    val needsApiKey: Boolean = false,
) {
    val hasApiKey: Boolean
        get() = !apiKey.isNullOrBlank()
    val isConfigured: Boolean get() = needsApiKey && hasApiKey || !needsApiKey
}

val wallpaperSourcesStatic = listOf<WallpaperSourceItem>(
    WallpaperSourceItem(
        id = 1,
        name = "Wallhaven",
        description = "A popular source for high-quality desktop wallpapers.",
        apiKeyDataStoreKey = UserPrefsKeys.WALLHAVEN_API_KEY,
        isDefault = true,
    ),
    WallpaperSourceItem(
        id = 2,
        name = "Pexels",
        description = "A collection of freely-usable high-resolution photos.",
        apiKeyDataStoreKey = UserPrefsKeys.PEXELS_API_KEY,
        needsApiKey = true
    ),
    WallpaperSourceItem(
        id = 3,
        name = "Unsplash",
        description = "A collection of freely-usable high-resolution photos.",
        apiKeyDataStoreKey = UserPrefsKeys.UNSPLASH_API_KEY,
        needsApiKey = true
    ),
)
