package com.ace.wallpaperrex.data.repositories

import com.ace.wallpaperrex.data.http.KtorClient
import com.ace.wallpaperrex.ui.models.ImageItem
import com.ace.wallpaperrex.ui.models.ImageResponse
import com.ace.wallpaperrex.ui.models.WallpaperSourceItem
import io.ktor.client.HttpClient

interface WallpaperRepository {
    /**
     * Searches for images based on a user-provided query.
     *
     * @param query The search term.
     * @return A [Result] containing an [ImageResponse] on success, or an [Exception] on failure.
     */
    suspend fun searchImages(
        page: Int,
        query: String,
        pageSize: Int = 20,
    ): Result<ImageResponse<ImageItem>>

    /**
     * Fetches a list of the latest or trending images, without a specific search query.
     *
     * @param sorting Defines the order of the images (e.g., "date_added", "toplist").
     * @return A [Result] containing an [ImageResponse] on success, or an [Exception] on failure.
     */
    suspend fun getImages(
        page: Int,
        sorting: String? = null,
        pageSize: Int = 20
    ): Result<ImageResponse<ImageItem>>
}

object WallpaperRepositoryProvider {
    fun provide(
        source: WallpaperSourceItem,
        client: HttpClient = KtorClient.instance
    ): WallpaperRepository {
        return when (source.id) {
            1 -> WallhavenImageRepository(client, apiKey = source.apiKey)
            else -> throw IllegalArgumentException("Unknown source ID: ${source.id}. No repository for the given source id")
        }
    }
}