package com.ace.wallpaperrex.data.repositories

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.ace.wallpaperrex.data.http.KtorClient
import com.ace.wallpaperrex.data.models.WallpaperSourceConfigItem
import com.ace.wallpaperrex.ui.models.ImageItem
import com.ace.wallpaperrex.ui.models.Meta
import com.ace.wallpaperrex.ui.models.PaginatedResponse
import com.ace.wallpaperrex.utils.mapToUserFriendlyException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.URLProtocol
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class WallpaperRepositoryImpl(
    val source: WallpaperSourceConfigItem,
    val httpClient: HttpClient = KtorClient.instance
) : WallpaperRepository {
    private val jsonParser = Json { ignoreUnknownKeys = true;isLenient = true }
    override suspend fun searchImages(
        page: Int,
        query: String,
        pageSize: Int
    ): Result<PaginatedResponse<ImageItem>> {
        val endpoint = source.api.endpoints.search
        return makePaginatedRequest(endpoint, page, pageSize) {
            parameter(source.api.searchParam, query)
        }

    }

    private suspend fun makePaginatedRequest(
        endpoint: String,
        page: Int,
        pageSize: Int,
        parameterBlock: HttpRequestBuilder.() -> Unit
    ): Result<PaginatedResponse<ImageItem>> {
        return try {
            val response: String = httpClient.get {
                url {
                    protocol = URLProtocol.HTTPS
                    host = source.api.domain
                    pathSegments += endpoint.trimStart('/').split("/")
                }
                applyAuth()
                parameter(source.api.pagination.pageParam, page)
                source.api.pagination.perPageParam?.let { param -> parameter(param, pageSize) }
                parameterBlock()
            }.body()

            val jsonObject = jsonParser.parseToJsonElement(response).jsonObject
            parsePaginatedResponse(jsonObject)
        } catch (e: Exception) {
            Result.failure(mapToUserFriendlyException(e))
        }
    }

    private fun parsePaginatedResponse(jsonObject: JsonObject): Result<PaginatedResponse<ImageItem>> {
        return runCatching {
            val mapping = source.responseMapping
            val resultList = jsonObject.extractJsonArray(mapping.resultListPath)
                ?: throw IllegalStateException("Results not found at path: ${mapping.resultListPath}")
            val imageItems = resultList.map { parseImageItem(it.jsonObject) }
            val currentPage = jsonObject.extractInt(mapping.pagination.currentPagePath)
                ?: throw IllegalStateException("Current page not found at path: ${mapping.pagination.currentPagePath}")
            val totalPages = jsonObject.extractInt(mapping.pagination.totalPath)
                ?: throw IllegalStateException("Total pages not found at path: ${mapping.pagination.totalPath}")
            val perPage = jsonObject.extractInt(mapping.pagination.perPagePath)
                ?: throw IllegalStateException("Per page not found at path: ${mapping.pagination.perPagePath}")

            val lastPage =
                mapping.pagination.lastPagePath?.let { jsonObject.extractInt(mapping.pagination.lastPagePath) }
            PaginatedResponse(
                data = imageItems, meta = Meta(
                    currentPage = currentPage,
                    lastPage = lastPage ?: ((totalPages + perPage - 1) / perPage),
                    perPage = perPage,
                    total = totalPages,
                )
            )
        }
    }

    private fun buildUrl(endpoint: String): String {
        val baseUrl = source.api.domain
        return "$baseUrl$endpoint"
    }

    private fun HttpRequestBuilder.applyAuth() {
        val auth = source.api.authentication
        if (source.apiKey.isNotBlank()) {
            val key = source.apiKey
            when (auth.type) {
                "query" -> parameter(auth.key, key)
                "header" -> header(auth.key, key)
            }
        }
    }

    private fun parseImageItem(jsonObject: JsonObject): ImageItem {
        val mapping = source.responseMapping.image
        return ImageItem(
            id = jsonObject.extractString(mapping.idPath)
                ?: throw IllegalStateException("ID not found at path: ${mapping.idPath}"),
            url = jsonObject.extractString(mapping.imageUrlPath)
                ?: throw IllegalStateException("Image URL not found at path: ${mapping.imageUrlPath}"),
            thumbnail = jsonObject.extractString(mapping.thumbnailUrlPath)
                ?: throw IllegalStateException("Thumbnail URL not found at path: ${mapping.thumbnailUrlPath}"),
            uploader = mapping.uploaderPath?.let { jsonObject.extractString(it) },
            uploaderUrl = mapping.uploaderUrlPath?.let { jsonObject.extractString(it) },
            extension = "webp",
            sourceKey = source.uniqueKey,
            alt = mapping.altPath?.let { jsonObject.extractString(it) },
            placeHolderColor = mapping.placeholderColorPath?.let {
                Color(jsonObject.extractString(it)!!.toColorInt())
            }
        )
    }

    override suspend fun getImages(
        page: Int,
        sorting: String?,
        pageSize: Int
    ): Result<PaginatedResponse<ImageItem>> {
        val endpoint = source.api.endpoints.curated
        return makePaginatedRequest(endpoint, page, pageSize) {
            //
        }
    }

    override suspend fun getSingleImage(id: String): Result<ImageItem> {
        val endpointTemplate = source.api.endpoints.detail
        val endpoint = endpointTemplate.replace("{id}", id)

        return try {
            val response: String = httpClient.get(
                urlString = buildUrl(endpoint)
            ) {
                url {
                    protocol = URLProtocol.HTTPS
                    host = source.api.domain
                    pathSegments += endpoint.trimStart('/').split("/")
                }
                applyAuth()
                applyAuth()
            }.body()

            val jsonObject = jsonParser.parseToJsonElement(response).jsonObject
            Result.success(parseImageItem(jsonObject))
        } catch (e: Exception) {
            Result.failure(mapToUserFriendlyException(e))
        }
    }

    private fun JsonElement.extractValue(path: String): JsonElement? {
        val parts = path.split('.')
        var current: JsonElement? = this
        for (part in parts) {
            current = current?.jsonObject?.get(part)
        }
        return current
    }

    private fun JsonObject.extractString(path: String): String? =
        extractValue(path)?.jsonPrimitive?.contentOrNull

    private fun JsonObject.extractInt(path: String): Int? =
        extractValue(path)?.jsonPrimitive?.intOrNull

    private fun JsonObject.extractJsonArray(path: String): JsonArray? =
        extractValue(path)?.jsonArray
}