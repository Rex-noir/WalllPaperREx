package com.ace.wallpaperrex.ui.screens.models

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.ace.wallpaperrex.data.models.WallpaperSourceConfigItem
import com.ace.wallpaperrex.ui.models.ImageItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Immutable
data class SharedViewModelUIState(
    val selectedImage: ImageItem? = null,
    val selectedSourceConfig: WallpaperSourceConfigItem? = null
)

class SharedViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SharedViewModelUIState())
    val uiState = _uiState.asStateFlow()

    fun setSelectedImage(image: ImageItem, sourceConfigItem: WallpaperSourceConfigItem) {
        _uiState.value = _uiState.value.copy(selectedImage = image, selectedSourceConfig = sourceConfigItem)
    }

    fun clearSelectedImage() {
        _uiState.value = _uiState.value.copy(selectedImage = null, selectedSourceConfig = null)
    }
}