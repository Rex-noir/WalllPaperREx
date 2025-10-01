package com.ace.wallpaperrex.data.repositories

import android.util.Log
import com.ace.wallpaperrex.data.ImageResponse
import com.ace.wallpaperrex.data.WallhavenSearchResponse
import com.ace.wallpaperrex.data.WallhavenWallpaperDetail
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

interface WallhavenImageRepository {
    suspend fun getImages(
        page: Int,
        pageSize: Int = 20,
        query: String? = null, // Optional: for search queries
        categories: String? = null, // Optional: e.g., "110" (General, Anime, People)
        purity: String? = null, // Optional: e.g., "100" (SFW)
        sorting: String? = "date_added", // Optional: e.g., "relevance", "random", "views", "favorites"
        order: String? = "desc", // Optional: "asc" or "desc"
        apiKey: String? = null // Optional: For higher rate limits or specific features
    ): Result<ImageResponse<WallhavenWallpaperDetail>>
}

class WallhavenImageRepositoryImpl : WallhavenImageRepository {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("KtorLogger", message)
                }
            }
            level = LogLevel.ALL
        }

        install(DefaultRequest) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }

    }

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
        apiKey: String?
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