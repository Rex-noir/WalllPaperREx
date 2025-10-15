package com.ace.wallpaperrex.data.repositories

import android.net.Uri
import android.util.Log
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ace.wallpaperrex.data.models.WallpaperSourceConfigItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class WallpaperSourceRepository(
    private val sourceRepository: SourcesRepository,
    private val dataStoreRepository: DataStoreRepository
) {

    val wallpaperSources: Flow<List<WallpaperSourceConfigItem>> = combine(
        sourceRepository.sourcesConfig,
        dataStoreRepository.wallpaperSourcesDataStore
    ) { configResult, prefs ->
        val config = configResult.getOrNull() ?: return@combine emptyList()



        config.sources.map { item ->
            val key = stringPreferencesKey(item.uniqueKey)
            val apiValue = prefs[key] ?: ""

            val safeModeEnabled =
                prefs[dataStoreRepository.getSafeModePrefsKey(uniqueKey = item.uniqueKey)]
            item.copy(
                apiKey = apiValue,
                api = item.api.copy(
                    safeMode = item.api.safeMode?.copy(
                        enabled = safeModeEnabled ?: item.api.safeMode.enabled
                    )
                )
            )
        }
    }

    val sourceError = sourceRepository.sourcesConfig.map { it.exceptionOrNull() }

    val lastWallpaperSource: Flow<WallpaperSourceConfigItem?> = combine(
        dataStoreRepository.wallpaperSourcesDataStore,
        wallpaperSources
    ) { prefs, allSources ->
        val lastSourceKey = prefs[DataStoreKeys.LAST_WALLPAPER_SOURCE_UNIQUE_KEY]
        Log.d("WallpaperSourceRepository", "Last source key: $lastSourceKey")
        allSources.find { it.uniqueKey == lastSourceKey }
    }

    suspend fun getWallpaperSource(key: String): WallpaperSourceConfigItem? =
        wallpaperSources.first().find { it.uniqueKey == key }

    suspend fun setWallpaperApiKey(item: WallpaperSourceConfigItem, apiKey: String) {
        val key = stringPreferencesKey(item.uniqueKey)
        dataStoreRepository.setWallpaperApiKey(key, apiKey)
    }

    suspend fun setLastWallpaperSource(key: String) {
        dataStoreRepository.setLastWallpaperSourceKey(key)
    }

    suspend fun updateSafeModeForSource(item: WallpaperSourceConfigItem, enabled: Boolean) {
        dataStoreRepository.updateSafeModeForSource(item.uniqueKey, enabled)
    }

    suspend fun updateSourcesFromNetwork(url: String): Result<Unit> =
        sourceRepository.updateFromNetwork(url)

    suspend fun importSourcesFromFile(uri: Uri): Result<Unit> = sourceRepository.importFromFile(uri)

    suspend fun resetSourceConfigToDefault(): Result<Unit> = sourceRepository.resetToDefault()
    suspend fun initialize() {
        sourceRepository.triggerInitialLoadI()
        Log.d("WallpaperSourceRepository", "Sources initialized: ${wallpaperSources.first()}")
    }

}