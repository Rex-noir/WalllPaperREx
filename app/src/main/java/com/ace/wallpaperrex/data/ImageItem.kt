package com.ace.wallpaperrex.data

import kotlinx.serialization.Serializable

@Serializable
data class ImageItem(
    val id: String,
    val urlSmall: String,
    val urlFull: String,
    val aspectRatio: Float = 1f,
    val description: String? = null
)
