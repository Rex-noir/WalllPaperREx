package com.ace.wallpaperrex.data.repositories

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.ace.wallpaperrex.data.http.KtorClient
import com.ace.wallpaperrex.data.models.WallpaperSourceConfigItem
import com.ace.wallpaperrex.ui.models.ImageItem
import com.ace.wallpaperrex.ui.models.Meta
import com.ace.wallpaperrex.ui.models.PaginatedResponse
import com.ace.wallpaperrex.utils.WallpaperHelper
import com.ace.wallpaperrex.utils.mapToUserFriendlyException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.Headers
import io.ktor.http.URLProtocol
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
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
        return makePaginatedRequest(
            endpoint,
            page,
            pageSize,
            resultListPath = source.responseMapping.resultListPaths.searchPath
        ) {
            parameter(source.api.searchParam, query)
        }

    }

    private suspend fun makePaginatedRequest(
        endpoint: String,
        page: Int,
        pageSize: Int,
        resultListPath: String?,
        parameterBlock: HttpRequestBuilder.() -> Unit
    ): Result<PaginatedResponse<ImageItem>> {
        return try {
            val response = httpClient.get {
                url {
                    protocol = URLProtocol.HTTPS
                    host = source.api.domain
                    pathSegments += endpoint.trimStart('/').split("/")
                }
                expectSuccess = true
                applyAuth()
                parameter(source.api.pagination.pageParam, page)
                source.api.pagination.perPageParam?.let { param -> parameter(param, pageSize) }
                parameterBlock()
            }
            val body = response.body<String>()
            val headers = response.headers

            val rootElement = jsonParser.parseToJsonElement(body)
            return when (rootElement) {
                is JsonObject -> parsePaginatedResponse(
                    rootElement,
                    headers,
                    resultListPath = resultListPath!!
                )

                is JsonArray -> {
                    val wrappedObject = buildJsonObject { put("data", rootElement) }
                    parsePaginatedResponse(wrappedObject, headers, resultListPath = "data")
                }

                else -> throw IllegalStateException("Unexpected root JSON element type: ${rootElement::class}")
            }
        } catch (e: Exception) {
            Result.failure(mapToUserFriendlyException(e))
        }
    }

    private fun parsePaginatedResponse(
        jsonObject: JsonObject,
        headers: Headers,
        resultListPath: String
    ): Result<PaginatedResponse<ImageItem>> {
        return runCatching {
            val mapping = source.responseMapping
            val resultList = jsonObject.extractJsonArray(resultListPath)
                ?: throw IllegalStateException("Results not found at path: ${mapping.resultListPaths}")
            val imageItems = resultList.map { parseImageItem(it.jsonObject) }

            var currentPage: Int? = null
            var totalPages: Int? = null
            var perPage: Int? = null
            var lastPage: Int? = null

            if (mapping.pagination.source == "response") {
                currentPage =
                    mapping.pagination.paths.currentPagePath?.let { jsonObject.extractInt(it) }

                totalPages = jsonObject.extractInt(mapping.pagination.paths.totalPath)

                perPage =
                    mapping.pagination.paths.perPagePath?.let { jsonObject.extractInt(it) }

                lastPage =
                    mapping.pagination.paths.lastPagePath?.let { jsonObject.extractInt(it) }
            } else if (mapping.pagination.source == "header") {
                currentPage = mapping.pagination.paths.currentPagePath
                    ?.let { path -> headers[path]?.toIntOrNull() }

                totalPages = mapping.pagination.paths.totalPath
                    .let { path -> headers[path]?.toIntOrNull() }

                perPage = mapping.pagination.paths.perPagePath
                    ?.let { path -> headers[path]?.toIntOrNull() }

                lastPage = mapping.pagination.paths.lastPagePath
                    ?.let { path -> headers[path]?.toIntOrNull() }

            }

            PaginatedResponse(
                data = imageItems, meta = Meta(
                    currentPage = currentPage,
                    lastPage = lastPage
                        ?: if (perPage != null && totalPages != null) ((totalPages.plus(perPage))
                            .minus(1)).div(perPage) else null,
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
        val width = jsonObject.extractInt(mapping.widthPath)
            ?: throw IllegalStateException("Width not found at path: ${mapping.widthPath}")
        val height = jsonObject.extractInt(mapping.heightPath)
            ?: throw IllegalStateException("Height not found at path: ${mapping.heightPath}")

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
            aspectRatio = width.toFloat() / height.toFloat(),
            placeHolderColor = mapping.placeholderColorPath?.let {
                when (val colorElement = jsonObject.extractValue(it)) {
                    is JsonPrimitive -> {
                        colorElement.contentOrNull?.runCatching {
                            Color(this.toColorInt())
                        }?.getOrNull()
                    }

                    is JsonArray -> {
                        val colorStrings: List<String> =
                            colorElement.mapNotNull { value -> value.jsonPrimitive.contentOrNull }

                        if (colorStrings.isNotEmpty()) {
                            WallpaperHelper.calculateAverageColor(colorStrings)
                        } else {
                            null
                        }
                    }

                    else -> null
                }
            }
        )
    }

    override suspend fun getImages(
        page: Int,
        sorting: String?,
        pageSize: Int
    ): Result<PaginatedResponse<ImageItem>> {
        val endpoint = source.api.endpoints.curated
        return makePaginatedRequest(
            endpoint,
            page,
            pageSize,
            resultListPath = source.responseMapping.resultListPaths.curatedPath
        ) {
            //
        }
    }

    override suspend fun getSingleImage(id: String): Result<ImageItem> {
        val endpointTemplate = source.api.endpoints.detail
        val endpoint = endpointTemplate?.replace("{id}", id)

        if (endpoint == null) {
            return Result.failure(IllegalStateException("Endpoint not found"))
        }

        return try {
            val response: String = httpClient.get {
                expectSuccess = true
                url {
                    protocol = URLProtocol.HTTPS
                    host = source.api.domain
                    pathSegments += endpoint.trimStart('/').split("/")
                }
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