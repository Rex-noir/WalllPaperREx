package com.ace.wallpaperrex.data.repositories

import com.ace.wallpaperrex.ui.screens.models.AutoChangeWallpaperSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

interface AutoChangeWallpaperSettingMethods {
    suspend fun updateAutoChangeWallpaperEnabled(enabled: Boolean): Unit
    suspend fun updateAutoChangeWallpaperInterval(interval: Int): Unit
    suspend fun updateAutoChangeWallpaperSource(source: String): Unit
    suspend fun updateAutoChangeWallpaperCustomSources(sources: String): Unit
}

class GeneralSettingsRepository(
    val dataStoreRepository: DataStoreRepository,
    val wallpaperSourceRepository: WallpaperSourceRepository
) : AutoChangeWallpaperSettingMethods {
    val autoChangeWallpaperSetting: Flow<AutoChangeWallpaperSetting> = combine(
        wallpaperSourceRepository.wallpaperSources,
        dataStoreRepository.autoChangeWallpaperDataStore
    ) { wallpaperSources, prefs ->
        val customSources =
            prefs[DataStoreKeys.AUTO_CHANGE_WALLPAPER_CUSTOM_SOURCES]?.split(",")
                ?.mapNotNull { customSourceKey ->
                    wallpaperSources.find { it.uniqueKey == customSourceKey }
                }

        val isEnabled = prefs[DataStoreKeys.AUTO_CHANGE_WALLPAPER_ENABLED_KEY] ?: false
        val interval = prefs[DataStoreKeys.AUTO_CHANGE_WALLPAPER_INTERVAL_KEY] ?: 15
        val sourceParsed =
            AutoChangeWallpaperSetting.Source.fromKey(prefs[DataStoreKeys.AUTO_CHANGE_WALLPAPER_SOURCE])
        val source =
            if (customSources?.isNotEmpty() == true && sourceParsed != null) {
                sourceParsed
            } else {
                AutoChangeWallpaperSetting.Source.FAVORITES
            }
        AutoChangeWallpaperSetting(
            enabled = isEnabled,
            interval = interval,
            source = source,
            customSources = customSources ?: emptyList()
        )
    }

    override suspend fun updateAutoChangeWallpaperEnabled(enabled: Boolean) {
        dataStoreRepository.updateAutoChangeWallpaperEnabled(enabled)
    }

    override suspend fun updateAutoChangeWallpaperInterval(interval: Int) {
        dataStoreRepository.updateAutoChangeWallpaperInterval(interval)
    }

    override suspend fun updateAutoChangeWallpaperSource(source: String) {
        dataStoreRepository.updateAutoChangeWallpaperSource(source)
    }

    override suspend fun updateAutoChangeWallpaperCustomSources(sources: String) {
        dataStoreRepository.updateAutoChangeWallpaperCustomSources(sources)
    }


}