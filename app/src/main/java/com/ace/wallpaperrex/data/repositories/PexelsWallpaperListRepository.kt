package com.ace.wallpaperrex.data.repositories

import com.ace.wallpaperrex.data.models.PexelsPaginatedResponse
import com.ace.wallpaperrex.data.models.PexelsWallpaperPhoto
import com.ace.wallpaperrex.ui.models.ImageItem
import com.ace.wallpaperrex.ui.models.PaginatedResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.http.path

class PexelsWallpaperRepository(
    private val client: HttpClient,
    private val apiKey: String
) : WallpaperRepository {

    private val baseUrl = "api.pexels.com"
    private val curatedPhotosPath = "/v1/curated"
    private val singlePhotoPath = "/v1/photos"

    private val searchPhotoPath = "/v1/search"
    override suspend fun searchImages(
        page: Int,
        query: String,
        pageSize: Int
    ): Result<PaginatedResponse<ImageItem>> {
        return try {
            val response: PexelsPaginatedResponse = client.get {
                url {
                    host = baseUrl
                    protocol = URLProtocol.HTTPS
                    path(searchPhotoPath)
                    parameters.append("query", query)
                    parameters.append("page", page.toString())
                    parameters.append("per_page", pageSize.toString())

                }
                headers {
                    append(HttpHeaders.Authorization, apiKey)
                }
            }.body()
            Result.success(
                response.toPaginatedResponse()
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getImages(
        page: Int,
        sorting: String?,
        pageSize: Int
    ): Result<PaginatedResponse<ImageItem>> {
        return try {
            val response: PexelsPaginatedResponse = client.get {
                url {
                    host = baseUrl
                    protocol = URLProtocol.HTTPS
                    path(curatedPhotosPath)
                    parameters.append("page", page.toString())
                    parameters.append("per_page", pageSize.toString())

                }
                headers {
                    append(HttpHeaders.Authorization, apiKey)
                }
            }.body()
            Result.success(
                response.toPaginatedResponse()
            )
        } catch (e: Exception) {
            Result.failure(e)
        }

    }

    override suspend fun getSingleImage(id: String): Result<ImageItem> {
        return try {
            val response: PexelsWallpaperPhoto = client.get {
                url {
                    host = baseUrl
                    protocol = URLProtocol.HTTPS
                    path("$singlePhotoPath/$id")
                }
                headers {
                    append(HttpHeaders.Authorization, apiKey)
                }
            }.body()
            Result.success(response.toImageItem())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}