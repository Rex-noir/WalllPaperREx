package com.ace.wallpaperrex.data.repositories

import android.util.Log
import com.ace.wallpaperrex.data.http.KtorClient
import com.ace.wallpaperrex.data.models.WallhavenSearchResponse
import com.ace.wallpaperrex.ui.models.ImageItem
import com.ace.wallpaperrex.ui.models.ImageResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.URLProtocol
import io.ktor.http.path

class WallhavenImageRepository(
    private val client: HttpClient = KtorClient.instance,
    private val apiKey: String?
) : WallpaperListRepository {

    private val baseUrl = "wallhaven.cc"
    private val apiPath = "/api/v1/search"

    /**
     * Implements the search contract. For Wallhaven, this simply calls the internal
     * fetch function with the provided query.
     */
    override suspend fun searchImages(
        page: Int,
        query: String,
        pageSize: Int
    ): Result<ImageResponse<ImageItem>> {
        // For search, sorting by "relevance" is often a good default.
        return fetch(page = page, query = query, sorting = "relevance")
    }

    /**
     * Implements the browsing contract. For Wallhaven, this calls the internal
     * fetch function with a null query and a specific sorting method.
     */
    override suspend fun getImages(
        page: Int,
        sorting: String?,
        pageSize: Int
    ): Result<ImageResponse<ImageItem>> {
        return fetch(page = page, query = null, sorting = sorting ?: "date_added")
    }

    /**
     * A private, shared function to handle the actual Ktor network call for Wallhaven,
     * as both searching and browsing use the same endpoint.
     */
    private suspend fun fetch(
        page: Int,
        query: String?,
        sorting: String?
    ): Result<ImageResponse<ImageItem>> {
        return try {
            val response: WallhavenSearchResponse = client.get {
                url {
                    protocol = URLProtocol.HTTPS
                    host = baseUrl
                    path(apiPath)

                    // Add parameters only if they have a value
                    if (!query.isNullOrBlank()) {
                        parameter("q", query)
                    }
                    if (!apiKey.isNullOrBlank()) {
                        parameter("apikey", apiKey)
                    }
                    parameter("page", page.toString())
                    parameter("sorting", sorting ?: "date_added")
                    parameter("order", "desc")
                    // Default parameters that can be overridden if needed in the future
                    parameter("categories", "111") // General, Anime, People
                    parameter("purity", "100") // SFW only
                }
            }.body()

            Result.success(
                ImageResponse(
                    data = response.data.map { it.toImageItem() },
                    meta = response.meta
                )
            )
        } catch (e: Exception) {
            Log.e("WallhavenRepo", "Error fetching images: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }
}
