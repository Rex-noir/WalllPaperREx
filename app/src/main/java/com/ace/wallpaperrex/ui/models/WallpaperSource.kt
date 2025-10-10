package com.ace.wallpaperrex.ui.models

import kotlinx.serialization.Serializable


@Serializable
data class WallpaperSourceConfigItem(
    val uniqueKey: String,
    val label: String,
    val description: String?,
    val supportApiKey: Boolean = true,
    val requireApiKey: Boolean = false,
    val documentationUrl: String?,
    val api: SourceApi,
    val responseMapping: SourceResponseMapping
)

@Serializable
data class SourceApi(
    val baseUrl: String,
    val endpoints: SourceEndpoints,
    val authentication: SourceAuthentication,
    val pagination: SourceApiPagination,
    val searchParam: String
)

@Serializable
data class SourceEndpoints(
    val search: String,
    val detail: String,
    val curated: String
)

@Serializable
data class SourceAuthentication(
    val type: String,
    val key: String
)

@Serializable
data class SourceApiPagination(
    val pageParam: String,
    val perPageParam: String,
    val currentPageParam: String
)

@Serializable
data class SourceResponseMapping(
    val resultListPath: String,
    val pagination: SourceResponsePagination,
    val image: SourceResponseImageMapping
)

@Serializable
data class SourceResponsePagination(
    val currentPagePath: String,
    val perPagePath: String,
    val totalPath: String
)

@Serializable
data class SourceResponseImageMapping(
    val idPath: String,
    val thumbnailUrlPath: String,
    val imageUrlPath: String,
    val descriptionPath: String?,
    val titlePath: String?,
    val uploaderPath: String?,
    val uploaderUrlPath: String?,
    val placeholderColorPath: String?
)