package com.ace.wallpaperrex.ui.screens.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ace.wallpaperrex.data.models.WallpaperSourceConfigItem
import com.ace.wallpaperrex.data.repositories.GeneralSettingsRepository
import com.ace.wallpaperrex.data.repositories.WallpaperSourceRepository
import com.ace.wallpaperrex.ui.screens.models.AutoChangeWallpaperSetting
import kotlinx.coroutines.launch


class GeneralSettingViewModel(
    sourcesRepository: WallpaperSourceRepository,
    private val generalSettingsRepository: GeneralSettingsRepository
) :
    ViewModel() {

    val wallpaperSources = sourcesRepository.wallpaperSources
    val autoChangeSetting =
        generalSettingsRepository.autoChangeWallpaperSetting

    fun updateAutoChangeWallpaperEnabled(enabled: Boolean) {
        viewModelScope.launch {
            generalSettingsRepository.updateAutoChangeWallpaperEnabled(enabled)
        }
    }

    fun setAutoChangeWallpaperInterval(interval: Int) {
        viewModelScope.launch {
            generalSettingsRepository.updateAutoChangeWallpaperInterval(interval)
        }
    }

    fun setAutoChangeWallpaperSource(source: AutoChangeWallpaperSetting.Source) {
        viewModelScope.launch {
            generalSettingsRepository.updateAutoChangeWallpaperSource(source.key)
        }
    }

    fun setAutoChangeCustomSources(sources: List<WallpaperSourceConfigItem>) {
        viewModelScope.launch {
            generalSettingsRepository.updateAutoChangeWallpaperCustomSources(sources.joinToString(",") { it.uniqueKey })
        }
    }

    companion object {
        const val TAG = "GeneralSettingViewModel"
        fun factory(
            wallpaperSourceRepository: WallpaperSourceRepository,
            generalSettingsRepository: GeneralSettingsRepository
        ): ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GeneralSettingViewModel(
                        wallpaperSourceRepository,
                        generalSettingsRepository
                    ) as T
                }
            }
        }


    }
}