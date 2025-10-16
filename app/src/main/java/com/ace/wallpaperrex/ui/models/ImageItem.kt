package com.ace.wallpaperrex.ui.models

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.ace.wallpaperrex.data.entities.FavoriteImageEntity
import com.ace.wallpaperrex.utils.WallpaperHelper
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class ImageItem(
    val id: String,
    val thumbnail: String,
    val url: String,
    val description: String? = null,
    val extension: String,
    val sourceKey: String,
    val alt: String? = null,
    val uploader: String? = null,
    val uploaderUrl: String? = null,
    val width: Int,
    val height: Int,
    @Serializable(with = WallpaperHelper.ColorSerializer::class)
    val placeHolderColor: Color?
) : BaseImage {
    override val aspectRatio: Float
        get() = width.toFloat() / height.toFloat()
}

fun ImageItem.toEntity(localPath: String? = null): FavoriteImageEntity {
    return FavoriteImageEntity(
        id = id,
        thumbnail = thumbnail,
        url = url,
        description = description,
        extension = extension,
        localPath = localPath,
        sourceKey = sourceKey,
        uploader = uploader,
        uploaderUrl = uploaderUrl,
        alt = alt,
        width = width,
        height = height,
    );
}

interface BaseImage {
    val aspectRatio: Float
}