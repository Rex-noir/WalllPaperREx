package com.ace.wallpaperrex.data

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class ImageItem(
    val id: String,
    val thumbnail: String,
    val url: String,
    val aspectRatio: Float = 1f,
    val description: String? = null
)

@Serializable
@Immutable
data class Meta(
    @SerialName("current_page") val currentPage: Int,
    @SerialName("last_page") val lastPage: Int,
    @SerialName("per_page") val perPage: Int,
    @SerialName("total") val total: Int
)

@Serializable
@Immutable
data class ImageResponse<T>(
    val data: List<T>,
    val meta: Meta?
)