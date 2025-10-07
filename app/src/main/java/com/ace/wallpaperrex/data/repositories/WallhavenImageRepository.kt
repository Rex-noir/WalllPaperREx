package com.ace.wallpaperrex.data.repositories

import android.util.Log
import com.ace.wallpaperrex.data.http.KtorClient
import com.ace.wallpaperrex.data.models.WallhavenSearchResponse
import com.ace.wallpaperrex.data.models.WallhavenWallpaperDetail
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
) :
    WallpaperListRepository {

    private val baseUrl = "wallhaven.cc"
    private val apiPath = "/api/v1/search"

    override suspend fun getImages(
        page: Int,
        pageSize: Int,
        query: String?,
        categories: String?,
        purity: String?,
        sorting: String?,
        order: String?,
    ): Result<ImageResponse<WallhavenWallpaperDetail>> {
        return try {
            val response: WallhavenSearchResponse = client.get {
                url {
                    protocol = URLProtocol.HTTPS
                    host = baseUrl
                    path(apiPath)

                    parameter("q", query)
                    parameter("page", page.toString())
                    parameter("sorting", sorting ?: "date_added")
                    parameter("order", order ?: "desc")
                    if (this@WallhavenImageRepository.apiKey != null) {
                        parameter("apiKey", apiKey)
                    }
                }
            }.body()

            Result.success(
                ImageResponse<WallhavenWallpaperDetail>(
                    data = response.data,
                    meta = response.meta
                )
            )
        } catch (e: Exception) {
            Log.e("WallhavenRepo", "Error fetching images : ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

}