package com.ace.wallpaperrex.data.daos

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.io.IOException

object UserPrefsKeys {
    val SELECTED_WALLPAPER_SOURCE_ID = intPreferencesKey("selected_wallpaper_source_id")
    val WALLHAVEN_API_KEY = stringPreferencesKey("wallpaper_api_key")
    val UNSPLASH_API_KEY = stringPreferencesKey("unsplash_api_key")
}

val Context.userPreferencesDataStore by preferencesDataStore(name = "user_preferences")

suspend fun Context.setSelectedWallpaperSourceId(sourceId: Int) {
    userPreferencesDataStore.edit { prefs ->
        prefs[UserPrefsKeys.SELECTED_WALLPAPER_SOURCE_ID] = sourceId
    }
}

fun Context.getSelectedWallpaperSourceId(): Flow<Int> {
    return userPreferencesDataStore.data.catch { e -> if (e is IOException) emit(emptyPreferences()) else e }
        .map { prefs -> prefs[UserPrefsKeys.SELECTED_WALLPAPER_SOURCE_ID] ?: 1 }
}


fun Context.getWallhavenApiKey(): Flow<String?> {
    return userPreferencesDataStore.data.catch { e -> if (e is IOException) emit(emptyPreferences()) else e }
        .map { preferences ->
            preferences[UserPrefsKeys.WALLHAVEN_API_KEY]
        }
}

suspend fun Context.setWallpaperApiKey(key: Preferences.Key<String>, apiKey: String) {
    userPreferencesDataStore.edit { preferences ->
        preferences[key] = apiKey
    }
}