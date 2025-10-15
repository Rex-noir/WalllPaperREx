package com.ace.wallpaperrex.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ace.wallpaperrex.ui.models.ImageItem

@Entity(
    tableName = "favorite_images",
    indices = [Index(value = ["savedAt"], orders = [Index.Order.DESC])]
)
data class FavoriteImageEntity(
    @PrimaryKey val id: String,
    val thumbnail: String,
    val url: String,
    val aspectRatio: Float = 0f,
    val description: String? = null,
    val extension: String,
    val localPath: String? = null,
    val savedAt: Long = System.currentTimeMillis(),
    val sourceKey: String,
    val uploader: String?,
    val uploaderUrl: String?,
    val alt: String? = null,
    val width: Int? = null,
    val height: Int? = null
)

fun FavoriteImageEntity.toImageItem(): ImageItem {
    return ImageItem(
        id = id,
        thumbnail = thumbnail,
        url = localPath!!,
        aspectRatio = aspectRatio,
        description = description,
        extension = extension,
        sourceKey = sourceKey,
        uploader = uploader,
        uploaderUrl = uploaderUrl,
        alt = alt,
        placeHolderColor = null
    )
}
