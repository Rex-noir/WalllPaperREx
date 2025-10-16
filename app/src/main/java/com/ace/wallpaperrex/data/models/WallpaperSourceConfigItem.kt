package com.ace.wallpaperrex.data.models


import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Serializable
data class WallpaperSourceConfigItem(
    val uniqueKey: String,
    val label: String,
    val description: String?,
    val supportApiKey: Boolean = true,
    val requireApiKey: Boolean = false,
    @Transient val apiKey: String = "",
    val documentationUrl: String?,
    val api: SourceApi,
    val responseMapping: SourceResponseMapping
) {
    val hasApiKey = apiKey.isNotEmpty() && supportApiKey
    val isConfigured get() = hasApiKey && requireApiKey || !requireApiKey
}

@Serializable
data class SourceApi(
    val domain: String,
    val endpoints: SourceEndpoints,
    val authentication: SourceAuthentication,
    val pagination: SourceApiPagination,
    val searchParam: String,
    val safeMode: SourceApiSafeMode? = null
)

@Serializable
data class SourceEndpoints(
    val search: String,
    val detail: String? = null,
    val curated: String,
    val download: String? = null
)

@Serializable
data class SourceApiSafeMode(
    val key: String,
    val type: String,
    val value: String,
    val enabled: Boolean = true
)

@Serializable
data class SourceAuthentication(
    val type: String,
    val key: String
)

@Serializable
data class SourceApiPagination(
    val pageParam: String,
    val perPageParam: String? = null,
    val currentPageParam: String? = null
)

@Serializable
data class SourceResponseMapping(
    val resultPaths: SourceResponseMappingResultList,
    val pagination: SourceResponsePagination,
    val image: SourceResponseImageMapping
)

@Serializable
data class SourceResponseMappingResultList(
    val curatedPath: String? = null,
    val searchPath: String? = null,
    val detailPath: String? = null
)

@Serializable
data class SourceResponsePagination(
    val source: String,
    val paths: SourceResponsePaginationPaths
)

@Serializable
data class SourceResponsePaginationPaths(
    val currentPagePath: String? = null,
    val perPagePath: String? = null,
    val totalPath: String,
    val lastPagePath: String? = null
)

@Serializable
data class SourceResponseImageMapping(
    val idPath: String,
    val thumbnailUrlPath: String,
    val imageUrlPath: String,
    val descriptionPath: String?,
    val altPath: String? = null,
    val uploaderPath: String? = null,
    val uploaderUrlPath: String? = null,
    val placeholderColorPath: String?,
    val widthPath: String,
    val heightPath: String
)
