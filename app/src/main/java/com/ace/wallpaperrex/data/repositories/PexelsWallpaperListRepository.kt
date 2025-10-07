package com.ace.wallpaperrex.data.repositories

import com.ace.wallpaperrex.ui.models.ImageItem
import com.ace.wallpaperrex.ui.models.ImageResponse
import io.ktor.client.HttpClient

class PexelsWallpaperListRepository(
    private val client: HttpClient,
    private val apiKey: String?
) : WallpaperListRepository {

    private val baseUrl = "api.pexels.com"
    private val apiPath = "/v1"
    override suspend fun searchImages(
        page: Int,
        query: String,
        pageSize: Int
    ): Result<ImageResponse<ImageItem>> {
        TODO("Not yet implemented")
    }

    override suspend fun getImages(
        page: Int,
        sorting: String?,
        pageSize: Int
    ): Result<ImageResponse<ImageItem>> {
        TODO("Not yet implemented")
    }


}