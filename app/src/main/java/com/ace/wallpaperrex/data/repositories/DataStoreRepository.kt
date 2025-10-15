package com.ace.wallpaperrex.data.repositories

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import okio.IOException

private val Context.userPreferencesDataStore by preferencesDataStore(name = "user_preferences")
private val Context.wallpaperSourcesDataStore by preferencesDataStore(name = "wallpaper_sources")
private val Context.autoChangeWallpaperDataStore by preferencesDataStore(name = "auto_change_wallpaper")

object DataStoreKeys {
    val LAST_WALLPAPER_SOURCE_UNIQUE_KEY = stringPreferencesKey("last_wallpaper_source_unique_key")
    val AUTO_CHANGE_WALLPAPER_ENABLED_KEY = booleanPreferencesKey("auto_change_wallpaper_enabled")
    val AUTO_CHANGE_WALLPAPER_INTERVAL_KEY = intPreferencesKey("auto_change_wallpaper_interval")

    val AUTO_CHANGE_WALLPAPER_SOURCE = stringPreferencesKey("auto_change_wallpaper_source")
    val AUTO_CHANGE_WALLPAPER_CUSTOM_SOURCES =
        stringPreferencesKey("auto_change_wallpaper_custom_sources")
}

class DataStoreRepository(
    private val context: Context
) : AutoChangeWallpaperSettingMethods {

    val wallpaperSourcesDataStore: Flow<Preferences> =
        context.wallpaperSourcesDataStore.data.catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }

    val autoChangeWallpaperDataStore: Flow<Preferences> =
        context.autoChangeWallpaperDataStore.data.catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }

    suspend fun setLastWallpaperSourceKey(uniqueKey: String) {
        context.wallpaperSourcesDataStore.edit {
            it[DataStoreKeys.LAST_WALLPAPER_SOURCE_UNIQUE_KEY] = uniqueKey
        }
    }

    suspend fun setWallpaperApiKey(key: Preferences.Key<String>, apiKey: String) {
        context.wallpaperSourcesDataStore.edit {
            it[key] = apiKey
        }
    }

    suspend fun updateSafeModeForSource(uniqueKey: String, safeMode: Boolean) {
        context.wallpaperSourcesDataStore.edit {
            it[getSafeModePrefsKey(uniqueKey)] = safeMode
        }
    }

    fun getSafeModePrefsKey(uniqueKey: String): Preferences.Key<Boolean> {
        return booleanPreferencesKey("$uniqueKey-safeMode")
    }

    override suspend fun updateAutoChangeWallpaperEnabled(enabled: Boolean) {
        context.autoChangeWallpaperDataStore.edit {
            it[DataStoreKeys.AUTO_CHANGE_WALLPAPER_ENABLED_KEY] = enabled
        }
    }

    override suspend fun updateAutoChangeWallpaperInterval(interval: Int) {
        context.autoChangeWallpaperDataStore.edit {
            it[DataStoreKeys.AUTO_CHANGE_WALLPAPER_INTERVAL_KEY] = interval
        }
    }

    override suspend fun updateAutoChangeWallpaperSource(source: String) {
        context.autoChangeWallpaperDataStore.edit {
            it[DataStoreKeys.AUTO_CHANGE_WALLPAPER_SOURCE] = source
        }
    }

    override suspend fun updateAutoChangeWallpaperCustomSources(sources: String) {
        context.autoChangeWallpaperDataStore.edit {
            it[DataStoreKeys.AUTO_CHANGE_WALLPAPER_CUSTOM_SOURCES] = sources
        }
    }

}