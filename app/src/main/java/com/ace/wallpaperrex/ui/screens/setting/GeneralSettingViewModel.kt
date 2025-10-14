package com.ace.wallpaperrex.ui.screens.setting

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Immutable
data class GeneralSettingState(
    val autoChangeWallpaper: Boolean = false,
    val autoChangeWallpaperInterval: Int = 15
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
            listOf<Int>(15, 30, 45, 60, 90, 120)
    }


}