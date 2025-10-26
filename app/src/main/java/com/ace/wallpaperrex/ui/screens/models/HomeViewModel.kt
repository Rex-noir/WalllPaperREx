package com.ace.wallpaperrex.ui.screens.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ace.wallpaperrex.data.repositories.DataStoreRepository
import com.ace.wallpaperrex.data.repositories.WallpaperSourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private val wallpaperSourceRepository: WallpaperSourceRepository
) : ViewModel() {
    val sources = wallpaperSourceRepository.wallpaperSources
    val sourceError = wallpaperSourceRepository.sourceError
    private val _configLoading = MutableStateFlow(true)
    val configLoading = _configLoading.asStateFlow()

    init {
        viewModelScope.launch {
            wallpaperSourceRepository.initialize()
            _configLoading.update { false }
        }
    }

    suspend fun resetConfigToDefault(): Result<Unit> {
        return wallpaperSourceRepository.resetSourceConfigToDefault()
    }
}