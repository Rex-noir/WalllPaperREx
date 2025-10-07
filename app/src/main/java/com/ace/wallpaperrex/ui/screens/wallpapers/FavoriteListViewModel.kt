package com.ace.wallpaperrex.ui.screens.wallpapers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.ace.wallpaperrex.data.database.AppDatabase
import com.ace.wallpaperrex.data.entities.FavoriteImageEntity
import com.ace.wallpaperrex.data.repositories.FavoriteImageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlin.reflect.KClass

class FavoriteListViewModel(private val favoriteImageRepository: FavoriteImageRepository) :
    ViewModel() {

    private val _favorites = MutableStateFlow<List<FavoriteImageEntity>>(mutableListOf())

    val favorites = _favorites.asStateFlow()

    init {
        favoriteImageRepository.allFavorites.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {

                val application = checkNotNull(extras[APPLICATION_KEY])
                val repository = FavoriteImageRepository(
                    dao = AppDatabase.getDatabase(application).favoriteImageDao()
                )
                return FavoriteListViewModel(repository) as T;
            }
        }
    }

}