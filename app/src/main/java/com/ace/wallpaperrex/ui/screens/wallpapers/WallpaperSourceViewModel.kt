package com.ace.wallpaperrex.ui.screens.wallpapers

import android.annotation.SuppressLint
import android.app.Application
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ace.wallpaperrex.data.daos.getDefaultWallpaperSourceId
import com.ace.wallpaperrex.data.daos.getWallhavenApiKey
import com.ace.wallpaperrex.data.daos.setDefaultWallpaperSourceId
import com.ace.wallpaperrex.data.daos.setWallpaperApiKey
import com.ace.wallpaperrex.ui.models.WallpaperSourceItem
import com.ace.wallpaperrex.ui.models.wallpaperSources
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WallpaperSourceViewModel(application: Application) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    private val _sources = MutableStateFlow(wallpaperSources)
    val sources: StateFlow<List<WallpaperSourceItem>> = _sources.asStateFlow()


    init {
        viewModelScope.launch {
            context.getDefaultWallpaperSourceId().collect { id ->
                _sources.value = _sources.value.map { it.copy(isDefault = it.id == id) }
            }

            context.getWallhavenApiKey().collect { key ->
                _sources.value = _sources.value.map { it.copy(apiKey = key) }
            }
        }
    }

    fun setAsDefault(id: Int) {
        viewModelScope.launch {
            context.setDefaultWallpaperSourceId(id)
        }
    }

    fun updateWallpaperApiKey(apiKeyDatastoreKey: Preferences.Key<String>, apiKey: String) {
        viewModelScope.launch {
            context.setWallpaperApiKey(apiKeyDatastoreKey, apiKey)
        }
    }
}