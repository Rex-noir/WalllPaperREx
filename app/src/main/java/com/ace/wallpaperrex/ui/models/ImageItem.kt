package com.ace.wallpaperrex.ui.models

import androidx.compose.runtime.Immutable
import com.ace.wallpaperrex.data.entities.FavoriteImageEntity
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class ImageItem(
    val id: String,
    val thumbnail: String,
    val url: String,
    val aspectRatio: Float = 0f,
    val description: String? = null,
    val extension: String,
    val sourceId: Int
)

fun ImageItem.toEntity(localPath: String? = null): FavoriteImageEntity {
    return FavoriteImageEntity(
        id = id,
        thumbnail = thumbnail,
        url = url,
        aspectRatio = aspectRatio,
        description = description,
        extension = extension,
        localPath = localPath,
        sourceId = sourceId
    );
}

interface ToImageItemMapper {
    fun toImageItem(): ImageItem
}