package com.ace.wallpaperrex.data.repositories

import android.net.Uri
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ace.wallpaperrex.data.models.WallpaperSourceConfig
import com.ace.wallpaperrex.data.models.WallpaperSourceConfigItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class WallpaperSourceRepository(
    private val sourceRepository: SourcesRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) {

    val wallpaperSources: Flow<List<WallpaperSourceConfigItem>> = combine(
        sourceRepository.sourcesConfig,
        userPreferencesRepository.userPreferencesFlow
    ) { configResult, prefs ->
        val config = configResult.getOrNull() ?: return@combine emptyList()
        val defaultKey = prefs[UserPrefsKeys.DEFAULT_WALLPAPER_SOURCE_UNIQUE_KEY]

        config.sources.map { item ->
            val key = stringPreferencesKey(item.uniqueKey)
            val apiValue = prefs[key] ?: ""
            item.copy(
                apiKey = apiValue,
                isDefault = defaultKey == item.uniqueKey
            )
        }.sortedByDescending { it.isDefault }
    }

    val lastWallpaperSource: Flow<WallpaperSourceConfigItem?> = combine(
        userPreferencesRepository.userPreferencesFlow,
        wallpaperSources
    ) { prefs, allSources ->
        val lastSourceKey = prefs[UserPrefsKeys.LAST_WALLPAPER_SOURCE_UNIQUE_KEY]
        allSources.find { it.uniqueKey === lastSourceKey }
    }

    suspend fun setDefaultWallpaperSource(item: WallpaperSourceConfigItem) {
        userPreferencesRepository.setDefaultWallpaperSourceKey(item.uniqueKey)
    }

    suspend fun setWallpaperApiKey(item: WallpaperSourceConfigItem, apiKey: String) {
        val key = stringPreferencesKey(item.uniqueKey)
        userPreferencesRepository.setWallpaperApiKey(key, apiKey)
    }

    suspend fun setLastWallpaperSource(key: String) {
        userPreferencesRepository.setLastWallpaperSourceKey(key)
    }

    suspend fun updateSourcesFromNetwork(url: String): Result<Unit> =
        sourceRepository.updateFromNetwork(url)

    suspend fun importSourcesFromFile(uri: Uri): Result<Unit> = sourceRepository.importFromFile(uri)

    suspend fun initialize() {
        sourceRepository.triggerInitialLoadI()
    }

}