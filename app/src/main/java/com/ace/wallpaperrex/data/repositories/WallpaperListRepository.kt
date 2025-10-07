package com.ace.wallpaperrex.data.repositories

import com.ace.wallpaperrex.data.http.KtorClient
import com.ace.wallpaperrex.data.models.WallhavenWallpaperDetail
import com.ace.wallpaperrex.ui.models.ImageResponse
import com.ace.wallpaperrex.ui.models.WallpaperSourceItem
import io.ktor.client.HttpClient

interface WallpaperListRepository {
    suspend fun getImages(
        page: Int,
        pageSize: Int = 20,
        query: String? = null, // Optional: for search queries
        categories: String? = null, // Optional: e.g., "110" (General, Anime, People)
        purity: String? = null, // Optional: e.g., "100" (SFW)
        sorting: String? = "date_added", // Optional: e.g., "relevance", "random", "views", "favorites"
        order: String? = "desc", // Optional: "asc" or "desc"
    ): Result<ImageResponse<WallhavenWallpaperDetail>>
}

object WallpaperListRepositoryProvider {
    fun provide(
        source: WallpaperSourceItem,
        client: HttpClient = KtorClient.instance
    ): WallpaperListRepository {
        return when (source.id) {
            1 -> WallhavenImageRepository(client, apiKey = source.apiKey)
            else -> throw IllegalArgumentException("Unknown source ID: ${source.id}")
        }
    }
}