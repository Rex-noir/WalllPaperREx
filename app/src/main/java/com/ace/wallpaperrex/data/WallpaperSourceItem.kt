package com.ace.wallpaperrex.data

data class WallpaperSourceItem(
    val id: Int,
    val name: String,
    val description: String,
    val isEnabled: Boolean = true,
    val apiKey: String? = ""
)

val wallpaperSources = listOf<WallpaperSourceItem>(
    WallpaperSourceItem(
        id = 1,
        name = "Wallhaven",
        description = "A popular source for high-quality desktop wallpapers."
    ),
    WallpaperSourceItem(
        id = 2,
        name = "Unsplash",
        description = "A collection of freely-usable high-resolution photos.",
        isEnabled = false
    ),
)