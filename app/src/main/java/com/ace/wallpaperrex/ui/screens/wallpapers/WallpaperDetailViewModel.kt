package com.ace.wallpaperrex.ui.screens.wallpapers

import android.app.Application
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.toRoute
import com.ace.wallpaperrex.AppRoute
import com.ace.wallpaperrex.data.database.AppDatabase
import com.ace.wallpaperrex.data.repositories.FavoriteImageRepository
import com.ace.wallpaperrex.ui.models.ImageItem
import com.ace.wallpaperrex.ui.models.toEntity
import com.ace.wallpaperrex.utils.ImageFileHelper.getImageBytesFromUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class WallpaperDetailViewModel(
    private val favoriteImageRepository: FavoriteImageRepository,
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

    fun addToFavorite(localPath: String) {
        val favImage = _image.value?.toEntity()?.copy(localPath = localPath)
        if (favImage !== null) {
            viewModelScope.launch(Dispatchers.IO) {
                favoriteImageRepository.addFavorite(favImage)
            }
        }
    }

    fun removeFromFavorite() {
        val favImage = _image.value?.toEntity()
        if (favImage !== null) {
            viewModelScope.launch(Dispatchers.IO) {
                favoriteImageRepository.removeFavorite(favImage)
            }
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

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                val savedStateHandle = extras.createSavedStateHandle()
                val repository = FavoriteImageRepository(
                    dao = AppDatabase.getDatabase(application).favoriteImageDao()
                )
                return WallpaperDetailViewModel(repository, savedStateHandle) as T
            }
        }
    }


}