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
    val sourceId: Int
)

fun FavoriteImageEntity.toImageItem(): ImageItem {
    return ImageItem(
        id = id,
        thumbnail = thumbnail,
        url = localPath!!,
        aspectRatio = aspectRatio,
        description = description,
        extension = extension,
        sourceId = 1
    )
}
