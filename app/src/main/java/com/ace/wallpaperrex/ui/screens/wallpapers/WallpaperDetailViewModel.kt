package com.ace.wallpaperrex.ui.screens.wallpapers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.ace.wallpaperrex.AppRoute
import com.ace.wallpaperrex.data.ImageItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class WallpaperDetailViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _image =
        MutableStateFlow<ImageItem?>(null)
    val imageItem: StateFlow<ImageItem?> = _image.asStateFlow()

    fun getImageId(): String? {
        return try {
            savedStateHandle.toRoute<AppRoute.WallpaperDetailRoute>().image
        } catch (e: Exception) {
            null
        }
    }


    fun setImage(image: ImageItem?) {
        _image.value = image
    }
}