package com.ace.wallpaperrex.data.repositories

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import okio.IOException

private val Context.userPreferencesDataStore by preferencesDataStore(name = "user_preferences")
private val Context.wallpaperSourcesDataStore by preferencesDataStore(name = "wallpaper_sources")

object DataStoreKeys {
    val LAST_WALLPAPER_SOURCE_UNIQUE_KEY = stringPreferencesKey("last_wallpaper_source_unique_key")
}

class DataStoreRepository(
    private val context: Context
) {

    val wallpaperSourcesDataStore: Flow<Preferences> =
        context.wallpaperSourcesDataStore.data.catch { e ->
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

}