package com.ace.wallpaperrex.data.repositories

import com.ace.wallpaperrex.ui.models.ImageItem
import com.ace.wallpaperrex.ui.models.PaginatedResponse
import com.ace.wallpaperrex.utils.WallpaperHelper
import com.ace.wallpaperrex.utils.mapToUserFriendlyException

interface WallpaperRepository {
    /**
     * Searches for images based on a user-provided query.
     *
     * @param query The search term.
     * @return A [Result] containing an [PaginatedResponse] on success, or an [Exception] on failure.
     */
    suspend fun searchImages(
        page: Int,
        query: String,
        pageSize: Int = 20,
    ): Result<PaginatedResponse<ImageItem>>

    /**
     * Fetches a list of the latest or trending images, without a specific search query.
     *
     * @param sorting Defines the order of the images (e.g., "date_added", "toplist").
     * @return A [Result] containing an [PaginatedResponse] on success, or an [Exception] on failure.
     */
    suspend fun getImages(
        page: Int,
        sorting: String? = null,
        pageSize: Int = WallpaperHelper.PER_PAGE_SIZE
    ): Result<PaginatedResponse<ImageItem>>

    suspend fun getSingleImage(id: String): Result<ImageItem>

    suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(mapToUserFriendlyException(e))
        }
    }

    suspend fun hitDownloadEndpoint(image: ImageItem): Result<Unit>
    suspend fun getRandomWallpaper(): Result<ImageItem>

}