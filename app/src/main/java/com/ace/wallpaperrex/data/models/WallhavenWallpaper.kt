package com.ace.wallpaperrex.data.models

import com.ace.wallpaperrex.ui.models.ImageItem
import com.ace.wallpaperrex.ui.models.Meta
import com.ace.wallpaperrex.ui.models.ToImageItemMapper
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WallhavenApiResponse(
    @SerialName("data") val data: WallhavenWallpaperDetail
)

@Serializable
data class WallhavenWallpaperDetail(
    @SerialName("id") val id: String,
    @SerialName("url") val url: String,
    @SerialName("short_url") val shortUrl: String,
    @SerialName("uploader") val uploader: Uploader? = null,
    @SerialName("views") val views: Int,
    @SerialName("favorites") val favorites: Int,
    @SerialName("source") val source: String? = null,
    @SerialName("purity") val purity: String,
    @SerialName("category") val category: String,
    @SerialName("dimension_x") val dimensionX: Int,
    @SerialName("dimension_y") val dimensionY: Int,
    @SerialName("resolution") val resolution: String,
    @SerialName("ratio") val ratio: String,
    @SerialName("file_size") val fileSize: Long,
    @SerialName("file_type") val fileType: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("colors") val colors: List<String>,
    @SerialName("path") val path: String,
    @SerialName("thumbs") val thumbs: Thumbs,
    @SerialName("tags") val tags: List<Tag> = emptyList()
) : ToImageItemMapper {
    override fun toImageItem(): ImageItem {
        return ImageItem(
            id = id,
            aspectRatio = ratio.toFloat(),
            url = path,
            thumbnail = thumbs.original,
            description = source,
            extension = "webp",
            sourceId = 1,
            uploader = uploader?.username,
            uploaderUrl = null
        )
    }
}

@Serializable
data class Uploader(
    @SerialName("username") val username: String,
    @SerialName("group") val group: String,
    @SerialName("avatar") val avatar: Avatar
)

@Serializable
data class Avatar(
    @SerialName("200px") val px200: String,
    @SerialName("128px") val px128: String,
    @SerialName("32px") val px32: String,
    @SerialName("20px") val px20: String
)

@Serializable
data class Thumbs(
    @SerialName("large") val large: String,
    @SerialName("original") val original: String,
    @SerialName("small") val small: String
)

@Serializable
data class Tag(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("alias") val alias: String,
    @SerialName("category_id") val categoryId: Int,
    @SerialName("category") val category: String,
    @SerialName("purity") val purity: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class WallhavenSearchResponse(
    @SerialName("data") val data: List<WallhavenWallpaperDetail>,
    @SerialName("meta") val meta: Meta? = null
)
