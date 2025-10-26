package com.ace.wallpaperrex.ui.screens.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ace.wallpaperrex.data.models.WallpaperSourceConfigItem
import com.ace.wallpaperrex.data.repositories.WallpaperSourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SourceSettingsViewModel @Inject constructor(
    private val wallpaperSourceRepository: WallpaperSourceRepository

) : ViewModel() {
    val sources = wallpaperSourceRepository.wallpaperSources;

    fun updateSafeModeForSource(source: WallpaperSourceConfigItem, safeMode: Boolean) {
        viewModelScope.launch {
            wallpaperSourceRepository.updateSafeModeForSource(source, safeMode)
        }
    }

    fun setWallpaperApiKey(source: WallpaperSourceConfigItem, apiKey: String) {
        viewModelScope.launch {
            wallpaperSourceRepository.setWallpaperApiKey(source, apiKey)
        }
    }
}