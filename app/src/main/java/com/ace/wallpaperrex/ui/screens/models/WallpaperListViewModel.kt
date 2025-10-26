package com.ace.wallpaperrex.ui.screens.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ace.wallpaperrex.data.repositories.WallpaperSourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WallpaperListViewModel @Inject constructor(
    val wallpaperRepository: WallpaperSourceRepository
) : ViewModel() {
    val sources = wallpaperRepository.wallpaperSources
    val lastWallpaperSource = wallpaperRepository.lastWallpaperSource

    fun setLastWallpaperSource(source: String) {
        viewModelScope.launch {
            wallpaperRepository.setLastWallpaperSource(source)
        }
    }
}