package com.ace.wallpaperrex.data.repositories

import com.ace.wallpaperrex.data.http.KtorClient
import com.ace.wallpaperrex.ui.models.ImageItem
import com.ace.wallpaperrex.ui.models.PaginatedResponse
import com.ace.wallpaperrex.ui.models.WallpaperSourceItem
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import java.io.IOException

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
        pageSize: Int = 20
    ): Result<PaginatedResponse<ImageItem>>

    suspend fun getSingleImage(id: String): Result<ImageItem>

    suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(mapToSpecificException(e))
        }
    }

    private fun mapToSpecificException(e: Exception): Exception {
        return when (e) {
            is ClientRequestException -> {
                when (e.response.status) {
                    HttpStatusCode.Unauthorized -> Exception(
                        "Invalid API Key. Please check your credentials.",
                        e
                    )

                    HttpStatusCode.BadRequest -> Exception(
                        "Invalid request. Please check the search query or parameters.",
                        e
                    )

                    HttpStatusCode.TooManyRequests -> Exception(
                        "You have exceeded the API rate limit.",
                        e
                    )

                    else -> Exception(
                        "Client error: ${e.response.status.description}. Please try again.",
                        e
                    )
                }
            }

            is ServerResponseException -> Exception(
                "Server error (${e.response.status.value}). Please try again later.",
                e
            )

            is IOException -> Exception(
                "Network error. Please check your internet connection.",
                e
            ) // For no internet
            else -> Exception(
                "An unexpected error occurred: ${e.message}",
                e
            ) // Fallback for other errors
        }
    }
}

object WallpaperRepositoryProvider {
    fun provide(
        source: WallpaperSourceItem,
        client: HttpClient = KtorClient.instance
    ): WallpaperRepository {
        return when (source.id) {
            1 -> WallhavenImageRepository(client, apiKey = source.apiKey)
            2 -> PexelsWallpaperRepository(client, apiKey = source.apiKey!!)
            else -> throw IllegalArgumentException("Unknown source ID: ${source.id}. No repository for the given source id")
        }
    }
}