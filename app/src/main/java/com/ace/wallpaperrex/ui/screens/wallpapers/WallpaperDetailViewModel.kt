package com.ace.wallpaperrex.ui.screens.wallpapers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.ace.wallpaperrex.AppRoute
import com.ace.wallpaperrex.ui.models.ImageItem
import com.ace.wallpaperrex.utils.ImageFileHelper.getImageBytesFromUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class WallpaperDetailViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _image =
        MutableStateFlow<ImageItem?>(null)
    val imageItem: StateFlow<ImageItem?> = _image.asStateFlow()

    private val _bytes = MutableStateFlow<ByteArray?>(null)

    val imageByes: StateFlow<ByteArray?> = _bytes.asStateFlow()


    fun getImageId(): String? {
        return try {
            savedStateHandle.toRoute<AppRoute.WallpaperDetailRoute>().image
        } catch (e: Exception) {
            null
        }
    }


    fun setImage(image: ImageItem?) {
        if (image == null) {
            _image.value = null
            _bytes.value = null
            return
        }

        _image.value = image
        viewModelScope.launch(Dispatchers.IO) {
            val bytes = getImageBytesFromUrl(image.url)

            withContext(Dispatchers.Main) {
                _bytes.value = bytes
            }
        }

    }
}