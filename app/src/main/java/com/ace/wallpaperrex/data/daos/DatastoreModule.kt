package com.ace.wallpaperrex.data.daos

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ace.wallpaperrex.ui.models.WallpaperSourceItem
import com.ace.wallpaperrex.ui.models.wallpaperSourcesStatic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.io.IOException

object UserPrefsKeys {
    val DEFAULT_WALLPAPER_SOURCE_ID = intPreferencesKey("default_wallpaper_id")
    val LAST_WALLPAPER_SOURCE_ID = intPreferencesKey("last_wallpaper_id")
    val WALLHAVEN_API_KEY = stringPreferencesKey("wallhaven_api_key")
    val UNSPLASH_API_KEY = stringPreferencesKey("unsplash_api_key")
    val PEXELS_API_KEY = stringPreferencesKey("pexels_api_key")
}

val Context.userPreferencesDataStore by preferencesDataStore(name = "user_preferences")

/**
 * Creates a single Flow that emits the fully constructed list of WallpaperSourceItem
 * based on the values stored in DataStore. This becomes the single source of truth.
 */
fun Context.getWallpaperSourcesFlow(): Flow<List<WallpaperSourceItem>> {
    return userPreferencesDataStore.data
        .catch { e ->
            if (e is IOException) {
                emit(emptyPreferences())
            } else {
                throw e
            }
        }
        .map { preferences ->
            val defaultId = preferences[UserPrefsKeys.DEFAULT_WALLPAPER_SOURCE_ID] ?: 1
            val wallhavenKey = preferences[UserPrefsKeys.WALLHAVEN_API_KEY] ?: ""
            val unsplashKey = preferences[UserPrefsKeys.UNSPLASH_API_KEY] ?: ""
            val pexelsKey = preferences[UserPrefsKeys.PEXELS_API_KEY] ?: ""

            // Use the hardcoded list as a template to construct the final list
            wallpaperSourcesStatic.map { source ->
                when (source.id) {
                    1 -> source.copy(
                        isDefault = source.id == defaultId,
                        apiKey = wallhavenKey
                    )

                    2 -> source.copy(
                        isDefault = source.id == defaultId,
                        apiKey = pexelsKey
                    )

                    else -> source
                }
            }.sortedByDescending { it.isDefault }
        }
}


fun Context.getLastWallpaperSource(): Flow<WallpaperSourceItem?> {
    return userPreferencesDataStore.data.catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .combine(getWallpaperSourcesFlow()) { preferences, allSources ->
            val lastSourceId = preferences[UserPrefsKeys.LAST_WALLPAPER_SOURCE_ID] ?: 1
            allSources.find { it.id == lastSourceId }
        }
}

fun Context.getDefaultWallpaperSource(): Flow<WallpaperSourceItem> {
    return userPreferencesDataStore.data.catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .combine(getWallpaperSourcesFlow()) { prefs, allSources ->
            val defaultSourceId = prefs[UserPrefsKeys.DEFAULT_WALLPAPER_SOURCE_ID] ?: 1
            allSources.find { it.id == defaultSourceId } ?: allSources.first()
        }
}

suspend fun Context.setLastWallpaperSourceId(id: Int) {
    userPreferencesDataStore.edit {
        it[UserPrefsKeys.LAST_WALLPAPER_SOURCE_ID] = id
    }
}

suspend fun Context.setDefaultWallpaperSourceId(sourceId: Int) {
    userPreferencesDataStore.edit { prefs ->
        prefs[UserPrefsKeys.DEFAULT_WALLPAPER_SOURCE_ID] = sourceId
    }
}

suspend fun Context.setWallpaperApiKey(key: Preferences.Key<String>, apiKey: String) {
    userPreferencesDataStore.edit { preferences ->
        preferences[key] = apiKey
    }
}
