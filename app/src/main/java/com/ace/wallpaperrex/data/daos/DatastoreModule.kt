package com.ace.wallpaperrex.data.daos

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.io.IOException

object UserPrefsKeys {
    val SELECTED_WALLPAPER_SOURCE_ID = intPreferencesKey("selected_wallpaper_source_id")
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