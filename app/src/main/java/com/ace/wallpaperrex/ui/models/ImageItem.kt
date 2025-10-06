package com.ace.wallpaperrex.ui.models

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class ImageItem(
    val id: String,
    val thumbnail: String,
    val url: String,
    val aspectRatio: Float = 0f,
    val description: String? = null,
    val extension: String
)

