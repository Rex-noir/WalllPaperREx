package com.ace.wallpaperrex.ui.screens.models

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.ace.wallpaperrex.data.database.AppDatabase
import com.ace.wallpaperrex.data.models.WallpaperSourceConfigItem
import com.ace.wallpaperrex.data.repositories.FavoriteImageRepository
import com.ace.wallpaperrex.data.repositories.WallpaperRepository
import com.ace.wallpaperrex.data.repositories.WallpaperRepositoryImpl
import com.ace.wallpaperrex.ui.models.ImageItem
import com.ace.wallpaperrex.ui.models.toEntity
import com.ace.wallpaperrex.utils.ImageFileHelper
import com.ace.wallpaperrex.utils.convertToWebpBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class WallpaperDetailViewModel(
    private val favoriteImageRepository: FavoriteImageRepository,
    private val image: ImageItem?,
    private val source: WallpaperSourceConfigItem,
    application: Application
) : AndroidViewModel(application) {

    private val _image =
        MutableStateFlow(image)
    val imageItem: StateFlow<ImageItem?> = _image.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _isSavingAsFavorite = MutableStateFlow(false)
    val isSavingAsFavorite: StateFlow<Boolean> = _isSavingAsFavorite.asStateFlow()

    private lateinit var repository: WallpaperRepository

    init {
        if (image != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val favImage = favoriteImageRepository.getById(image.id)
                _isFavorite.value = favImage != null

                repository = WallpaperRepositoryImpl(source)

                val detailedImage = repository.getSingleImage(image.id)
                if (detailedImage.isSuccess) {
                    _image.value = detailedImage.getOrThrow()
                }
            }
        }
    }

    private fun addToFavorite(localPath: String) {
        val favImage = _image.value?.toEntity()?.copy(localPath = localPath)
        Log.d("WallpaperDetailViewModel", "addToFavorite: $favImage")
        if (favImage !== null) {
            viewModelScope.launch(Dispatchers.IO) {
                favoriteImageRepository.addFavorite(favImage)
            }
        }
    }

    private fun removeFromFavorite() {
        val favImage = _image.value?.toEntity()
        if (favImage !== null) {
            viewModelScope.launch(Dispatchers.IO) {
                favoriteImageRepository.removeFavorite(favImage)
            }
        }
    }

    fun toggleFavoriteState(context: Context, bitmap: Bitmap, name: String) {
        viewModelScope.launch {
            val newValue = !_isFavorite.value
            _isFavorite.value = newValue

            if (newValue) {
                _isSavingAsFavorite.value = true
                viewModelScope.launch {
                    val localPath = ImageFileHelper.saveBytesToCache(
                        context,
                        name = name,
                        bytes = bitmap.convertToWebpBytes()
                    )
                    addToFavorite(localPath)
                    _isSavingAsFavorite.value = false
                }
            } else {
                ImageFileHelper.deleteCachedImage(context, name)
                removeFromFavorite()
            }
        }
    }

    companion object {

        fun createFactory(imageItem: ImageItem, sourceConfigItem: WallpaperSourceConfigItem) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    val application = checkNotNull(extras[APPLICATION_KEY])
                    val repository = FavoriteImageRepository(
                        dao = AppDatabase.getDatabase(application).favoriteImageDao()
                    )

                    return WallpaperDetailViewModel(
                        repository,
                        imageItem,
                        sourceConfigItem,
                        application
                    ) as T
                }
            }
    }


}