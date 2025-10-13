package com.ace.wallpaperrex.data.models

import com.ace.wallpaperrex.ui.models.WallpaperSourceConfigItem
import kotlinx.serialization.Serializable


@Serializable
data class WallpaperSourceConfig(
    val sources: List<WallpaperSourceConfigItem>
)