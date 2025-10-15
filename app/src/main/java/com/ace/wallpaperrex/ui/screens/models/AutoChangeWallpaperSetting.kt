package com.ace.wallpaperrex.ui.screens.models

import androidx.compose.runtime.Immutable
import com.ace.wallpaperrex.data.models.WallpaperSourceConfigItem

@Immutable
data class AutoChangeWallpaperSetting(
    val enabled: Boolean = true,
    val source: Source = Source.FAVORITES,
    val customSources: List<WallpaperSourceConfigItem> = emptyList(),
    val interval: Int = 15,
) {
    enum class Source(val key: String) {
        FAVORITES("favorites"),
        CUSTOM_SOURCES("custom_sources");

        companion object {
            fun fromKey(key: String?): Source? =
                entries.find { it.key == key }
        }
    }
}