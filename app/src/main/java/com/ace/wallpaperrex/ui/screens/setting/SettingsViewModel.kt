package com.ace.wallpaperrex.ui.screens.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ace.wallpaperrex.ui.models.WallpaperSourceItem
import com.ace.wallpaperrex.ui.models.wallpaperSources
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {
    private val _wallpaperSourcesState = MutableStateFlow<List<WallpaperSourceItem>>(emptyList())
    val wallpaperSourcesState: StateFlow<List<WallpaperSourceItem>> =
        _wallpaperSourcesState.asStateFlow()

    init {
        _wallpaperSourcesState.value = wallpaperSources.map { it }
    }

    fun toggleSourceEnabled(sourceId: Int) {
        viewModelScope.launch {
            _wallpaperSourcesState.update { currentSources ->
                currentSources.map { source ->
                    if (source.id == sourceId) {
                        source.copy(isEnabled = !source.isEnabled)
                    } else {
                        source
                    }
                }

                // TODO : Persist the change
            }
        }
    }

    private fun persistSourceConfiguration(source: WallpaperSourceItem) {
        // This is where you would save the source.isEnabled state
        // e.g., using SharedPreferences:
        // sharedPreferences.edit().putBoolean("source_${source.id}_enabled", source.isEnabled).apply()
        println("Configuration for ${source.name} updated: isEnabled = ${source.isEnabled}")
    }

    fun saveApiKey(sourceId: Int, apiKey: String) {
        // Save the api key to some some thing
    }

}