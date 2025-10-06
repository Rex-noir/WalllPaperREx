package com.ace.wallpaperrex.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_images")
data class FavoriteImageEntity(
    @PrimaryKey val id: String,
    val thumbnail: String,
    val url: String,
    val aspectRatio: Float = 0f,
    val description: String? = null,
    val extension: String,
    val localPath: String? = null,
    val savedAt: Long = System.currentTimeMillis()
)