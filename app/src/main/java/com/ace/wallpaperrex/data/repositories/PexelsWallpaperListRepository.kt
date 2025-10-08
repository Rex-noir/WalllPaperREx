package com.ace.wallpaperrex.data.repositories

import com.ace.wallpaperrex.data.models.PexelsPaginatedResponse
import com.ace.wallpaperrex.data.models.PexelsWallpaperPhoto
import com.ace.wallpaperrex.ui.models.ImageItem
import com.ace.wallpaperrex.ui.models.PaginatedResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
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
        return safeApiCall {
            val response: PexelsPaginatedResponse = client.get {
                expectSuccess = true
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
            response.toPaginatedResponse()
        }
    }

    override suspend fun getImages(
        page: Int,
        sorting: String?,
        pageSize: Int
    ): Result<PaginatedResponse<ImageItem>> {
        return safeApiCall {
            val response: PexelsPaginatedResponse = client.get {
                expectSuccess = true
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
            response.toPaginatedResponse()
        }
    }

    override suspend fun getSingleImage(id: String): Result<ImageItem> {
        return safeApiCall {
            val response: PexelsWallpaperPhoto = client.get {
                expectSuccess = true
                url {
                    host = baseUrl
                    protocol = URLProtocol.HTTPS
                    path("$singlePhotoPath/$id")
                }
                headers {
                    append(HttpHeaders.Authorization, apiKey)
                }
            }.body()
            response.toImageItem()

        }
    }

}