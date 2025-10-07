package com.ace.wallpaperrex.ui.screens.wallpapers

import android.annotation.SuppressLint
import android.app.Application
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ace.wallpaperrex.data.daos.getSelectedWallpaperSourceId
import com.ace.wallpaperrex.data.daos.getWallhavenApiKey
import com.ace.wallpaperrex.data.daos.setSelectedWallpaperSourceId
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

    private val _selectedSourceId = MutableStateFlow(1)
    val selectedSourceId: StateFlow<Int> = _selectedSourceId.asStateFlow()

    init {
        viewModelScope.launch {
            context.getSelectedWallpaperSourceId().collect { id ->
                _selectedSourceId.value = 1
            }

            context.getWallhavenApiKey().collect { key ->
                _sources.value = _sources.value.map { it.copy(apiKey = key) }
            }
        }
    }

    fun selectSource(sourceId: Int) {
        viewModelScope.launch {
            context.setSelectedWallpaperSourceId(sourceId = sourceId)
        }
    }

    fun updateWallpaperApiKey(apiKeyDatastoreKey: Preferences.Key<String>, apiKey: String) {
        viewModelScope.launch {
            context.setWallpaperApiKey(apiKeyDatastoreKey, apiKey)
        }
    }
}