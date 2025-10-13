package com.ace.wallpaperrex.data.repositories

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import okio.IOException

private val Context.userPreferencesDataStore by preferencesDataStore(name = "user_preferences")

object UserPrefsKeys {
    val DEFAULT_WALLPAPER_SOURCE_UNIQUE_KEY =
        stringPreferencesKey("default_wallpaper_unique_key")
    val LAST_WALLPAPER_SOURCE_UNIQUE_KEY = stringPreferencesKey("last_wallpaper_source_unique_key")
}

class UserPreferencesRepository(
    private val context: Context
) {

    val userPreferencesFlow: Flow<Preferences> = context.userPreferencesDataStore.data.catch { e ->
        if (e is IOException) emit(emptyPreferences()) else throw e
    }

    suspend fun setLastWallpaperSourceKey(uniqueKey: String) {
        context.userPreferencesDataStore.edit {
            it[UserPrefsKeys.LAST_WALLPAPER_SOURCE_UNIQUE_KEY] = uniqueKey
        }
    }

    suspend fun setDefaultWallpaperSourceKey(uniqueKey: String) {
        context.userPreferencesDataStore.edit {
            it[UserPrefsKeys.DEFAULT_WALLPAPER_SOURCE_UNIQUE_KEY] = uniqueKey
        }
    }

    suspend fun setWallpaperApiKey(key: Preferences.Key<String>, apiKey: String) {
        context.userPreferencesDataStore.edit {
            it[key] = apiKey
        }
    }
}