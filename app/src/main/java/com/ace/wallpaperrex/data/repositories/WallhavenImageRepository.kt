package com.ace.wallpaperrex.data.repositories

import android.util.Log
import com.ace.wallpaperrex.data.models.WallhavenApiResponse
import com.ace.wallpaperrex.data.models.WallhavenSearchResponse
import com.ace.wallpaperrex.ui.models.ImageItem
import com.ace.wallpaperrex.ui.models.PaginatedResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.URLProtocol
import io.ktor.http.path

class WallhavenImageRepository(
    private val client: HttpClient, // Removed default client to enforce dependency injection
    private val apiKey: String?
) : WallpaperRepository {

    private val baseUrl = "wallhaven.cc"
    private val searchPath = "/api/v1/search"
    private val singleImagePath = "/api/v1/w/"

    override suspend fun searchImages(
        page: Int,
        query: String,
        pageSize: Int
    ): Result<PaginatedResponse<ImageItem>> {
        return fetch(page = page, query = query, sorting = "relevance")
    }

    override suspend fun getImages(
        page: Int,
        sorting: String?,
        pageSize: Int
    ): Result<PaginatedResponse<ImageItem>> {
        return fetch(page = page, query = null, sorting = sorting ?: "date_added")
    }

    override suspend fun getSingleImage(id: String): Result<ImageItem> {
        // Use the safeApiCall from the interface
        return safeApiCall {
            val response: WallhavenApiResponse = client.get {
                expectSuccess = true
                url {
                    protocol = URLProtocol.HTTPS
                    host = baseUrl
                    path("$singleImagePath$id")
                    if (!apiKey.isNullOrBlank()) {
                        parameter("apikey", apiKey)
                    }
                }
            }.body()
            response.data.toImageItem()
        }.onFailure {
            // Optional: Log the failure from the specific repository
            Log.e("WallhavenRepo", "Error fetching single image '$id': ${it.message}", it)
        }
    }

    private suspend fun fetch(
        page: Int,
        query: String?,
        sorting: String?
    ): Result<PaginatedResponse<ImageItem>> {
        return safeApiCall {
            val response: WallhavenSearchResponse = client.get {
                expectSuccess = true
                url {
                    protocol = URLProtocol.HTTPS
                    host = baseUrl
                    path(searchPath)

                    if (!query.isNullOrBlank()) parameter("q", query)
                    if (!apiKey.isNullOrBlank()) parameter("apikey", apiKey)
                    parameter("page", page.toString())
                    parameter("sorting", sorting ?: "date_added")
                    parameter("order", "desc")
                    parameter("categories", "111")
                    parameter("purity", "100")
                }
            }.body()

            PaginatedResponse(
                data = response.data.map { it.toImageItem() },
                meta = response.meta
            )
        }.onFailure {
            // Optional: Log the failure from the specific repository
            Log.e("WallhavenRepo", "Error fetching image list: ${it.message}", it)
        }
    }
}
