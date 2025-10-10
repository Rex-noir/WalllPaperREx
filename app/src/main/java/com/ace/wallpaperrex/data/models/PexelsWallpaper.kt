package com.ace.wallpaperrex.data.models

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.ace.wallpaperrex.ui.models.ImageItem
import com.ace.wallpaperrex.ui.models.Meta
import com.ace.wallpaperrex.ui.models.PaginatedResponse
import com.ace.wallpaperrex.ui.models.PaginatedResponseMapper
import com.ace.wallpaperrex.ui.models.ToImageItemMapper
import kotlinx.serialization.Serializable

@Serializable
data class PexelsWallpaperPhoto(
    val id: String,
    val width: Int,
    val height: Int,
    val url: String,
    val photographer: String,
    val photographer_url: String,
    val photographer_id: Long,
    val avg_color: String,
    val src: PexelsWallpaperSource,
) : ToImageItemMapper {
    override fun toImageItem(): ImageItem {
        return ImageItem(
            id = id.toString(),
            url = src.original,
            aspectRatio = width.toFloat() / height.toFloat(),
            thumbnail = src.medium,
            description = photographer,
            extension = "webp",
            sourceId = 2,
            uploader = photographer,
            uploaderUrl = photographer_url,
            placeHolderColor = Color(avg_color.toColorInt()),
        )
    }
}

@Serializable
data class PexelsWallpaperSource(
    val original: String,
    val large2x: String,
    val large: String,
    val medium: String,
    val small: String,
    val portrait: String,
    val landscape: String,
    val tiny: String
)

@Serializable
data class PexelsPaginatedResponse(
    val photos: List<PexelsWallpaperPhoto>,
    val page: Int,
    val per_page: Int,
    val total_results: Int,
    val prev_page: String? = null,
    val next_page: String? = null
) : PaginatedResponseMapper<ImageItem> {
    override fun toPaginatedResponse(): PaginatedResponse<ImageItem> {
        return PaginatedResponse<ImageItem>(
            data = photos.map { it.toImageItem() },
            meta = Meta(
                currentPage = page,
                lastPage = total_results / per_page,
                perPage = per_page,
                total = total_results
            )
        )
    }
}