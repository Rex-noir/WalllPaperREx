package com.ace.wallpaperrex.ui.screens.setting

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ace.wallpaperrex.data.repositories.GeneralSettingRepository
import com.ace.wallpaperrex.data.repositories.WallpaperSourceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Immutable
data class GeneralSettingState(
    val autoChangeWallpaper: Boolean = true,
    val autoChangeWallpaperInterval: Int = 15,
    val autoChangeWallpaperSource: GeneralSettingRepository.Companion.AutoChangeWallpaperSource = GeneralSettingRepository.Companion.AutoChangeWallpaperSource.FAVORITES,
    val autoChangeCustomSources: List<String> = emptyList(),
    val autoChangeSafeMode: Boolean = true
)

class GeneralSettingViewModel(private val sourcesRepository: WallpaperSourceRepository) :
    ViewModel() {
    private val _state = MutableStateFlow(GeneralSettingState())
    val state = _state.asStateFlow()

    val wallpaperSources = sourcesRepository.wallpaperSources

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

        fun factory(wallpaperSourceRepository: WallpaperSourceRepository): ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GeneralSettingViewModel(wallpaperSourceRepository) as T
                }
            }
        }

    }

    fun setAutoChangeWallpaperSource(source: GeneralSettingRepository.Companion.AutoChangeWallpaperSource) {
        _state.value = _state.value.copy(autoChangeWallpaperSource = source)
    }

    fun setAutoChangeCustomSources(sources: List<String>) {
        _state.value = _state.value.copy(autoChangeCustomSources = sources)
    }

    fun setAutoChangeSafeMode(enabled: Boolean) {
        _state.value = _state.value.copy(autoChangeSafeMode = enabled)
    }

}