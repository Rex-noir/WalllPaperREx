package com.ace.wallpaperrex.data.repositories

import com.ace.wallpaperrex.data.models.WallpaperSourceConfigItem
import com.ace.wallpaperrex.ui.models.ImageItem
import com.ace.wallpaperrex.ui.models.PaginatedResponse

class WallpaperRepositoryImpl (val source: WallpaperSourceConfigItem): WallpaperRepository{
    override suspend fun searchImages(
        page: Int,
        query: String,
        pageSize: Int
    ): Result<PaginatedResponse<ImageItem>> {
        TODO("Not yet implemented")
    }

    override suspend fun getImages(
        page: Int,
        sorting: String?,
        pageSize: Int
    ): Result<PaginatedResponse<ImageItem>> {
        TODO("Not yet implemented")
    }

    override suspend fun getSingleImage(id: String): Result<ImageItem> {
        TODO("Not yet implemented")
    }
}