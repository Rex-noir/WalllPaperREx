package com.ace.wallpaperrex.data.models

import kotlinx.serialization.Serializable


@Serializable
data class WallpaperSourceConfig(
    val sources: List<WallpaperSourceConfigItem>
)