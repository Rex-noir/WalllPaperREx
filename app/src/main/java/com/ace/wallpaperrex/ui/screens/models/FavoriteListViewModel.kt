package com.ace.wallpaperrex.ui.screens.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.ace.wallpaperrex.data.database.AppDatabase
import com.ace.wallpaperrex.data.entities.toImageItem
import com.ace.wallpaperrex.data.repositories.FavoriteImageRepository
import com.ace.wallpaperrex.ui.models.ImageItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.reflect.KClass

class FavoriteListViewModel(private val favoriteImageRepository: FavoriteImageRepository) :
    ViewModel() {

    val favorites: StateFlow<List<ImageItem>> =
        favoriteImageRepository.getAllFavorites()
            .map { it.map { val toImageItem = it.toImageItem()
                toImageItem
            } }
            .stateIn(
                scope = viewModelScope,
                // Keep the upstream flow active for 5 seconds after the last collector disappears
                started = SharingStarted.WhileSubscribed(5000),
                // The initial value to be used while waiting for the first value from the flow
                initialValue = emptyList()
            )


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