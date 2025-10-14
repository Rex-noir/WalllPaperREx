package com.ace.wallpaperrex.ui.screens.setting

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.ace.wallpaperrex.data.repositories.GeneralSettingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Immutable
data class GeneralSettingState(
    val autoChangeWallpaper: Boolean = false,
    val autoChangeWallpaperInterval: Int = 15,
    val autoChangeWallpaperSource: GeneralSettingRepository.Companion.AutoChangeWallpaperSource = GeneralSettingRepository.Companion.AutoChangeWallpaperSource.FAVORITES,
    val autoChangeCustomSources: List<String> = emptyList()
)

class GeneralSettingViewModel : ViewModel() {
    private val _state = MutableStateFlow(GeneralSettingState())
    val state = _state.asStateFlow()

    fun setAutoChangeWallpaper(enabled: Boolean) {
        _state.value = _state.value.copy(autoChangeWallpaper = enabled)
    }

    fun setAutoChangeWallpaperInterval(interval: Int) {
        _state.value = _state.value.copy(autoChangeWallpaperInterval = interval)
    }

    companion object {
        const val TAG = "GeneralSettingViewModel"

        // In minutes
        val autoChangeWallpaperPeriodAvailableList =
            listOf<Int>(15, 30, 45, 60)

    }

    fun setAutoChangeWallpaperSource(source: GeneralSettingRepository.Companion.AutoChangeWallpaperSource) {
        _state.value = _state.value.copy(autoChangeWallpaperSource = source)
    }

    fun setAutoChangeCustomSources(sources: List<String>) {
        _state.value = _state.value.copy(autoChangeCustomSources = sources)
    }


}