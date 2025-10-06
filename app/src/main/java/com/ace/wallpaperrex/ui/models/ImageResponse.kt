package com.ace.wallpaperrex.ui.models

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
