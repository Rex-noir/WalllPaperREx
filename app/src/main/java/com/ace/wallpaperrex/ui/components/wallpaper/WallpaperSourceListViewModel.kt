package com.ace.wallpaperrex.ui.components.wallpaper

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.toRoute
import com.ace.wallpaperrex.data.daos.getWallpaperSourcesFlow
import com.ace.wallpaperrex.data.repositories.WallpaperRepository
import com.ace.wallpaperrex.data.repositories.WallpaperRepositoryProvider
import com.ace.wallpaperrex.ui.models.ImageItem
import com.ace.wallpaperrex.ui.screens.wallpapers.WallpaperSingleListRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class WallpaperListUiState(
    val items: List<ImageItem>? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val currentPage: Int = 1,
    val isEndOfList: Boolean = true,
    val currentQuery: String = "nature"
)

class WallpaperSourceListViewModel(
    val sourceId: Int,
    val repository: WallpaperRepository,
) :
    ViewModel() {

    private val _uiState = MutableStateFlow(WallpaperListUiState())
    val uiState: StateFlow<WallpaperListUiState> = _uiState.asStateFlow()

    init {
        loadWallpapers(page = 1, isInitialLoad = true)
    }


    fun loadWallpapers(
        page: Int,
        query: String? = "nature",
        isInitialLoad: Boolean = false,
        isSearchWipe: Boolean = false
    ) {

        if (_uiState.value.isLoading && (_uiState.value.isEndOfList && !isInitialLoad && isSearchWipe)) {
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                error = null,
                currentQuery = query ?: it.currentQuery
            )
        }

        viewModelScope.launch {
            val result = repository.getImages(
                page = page
            );
            result.fold(onSuccess = { imageResponse ->
                val newUiItems = imageResponse.data

                _uiState.update { currentState ->
                    val combinedItems = if (isInitialLoad || isSearchWipe) {
                        newUiItems
                    } else {
                        currentState.items?.plus(newUiItems) ?: newUiItems
                    }
                    currentState.copy(
                        items = combinedItems,
                        isLoading = false,
                        currentPage = imageResponse.meta?.currentPage?.plus(1)
                            ?: (currentState.currentPage + 1),
                        isEndOfList = newUiItems.isEmpty() || (imageResponse.meta?.currentPage == imageResponse.meta?.lastPage)
                    )
                }
            }, onFailure = { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.localizedMessage ?: "An unknown error occurred"
                    )
                }
            })
        }
    }

    fun searchWallpapers(query: String) {
        _uiState.update {
            it.copy(
                items = emptyList(), currentPage = 1, isEndOfList = false, currentQuery = query,
            )
        }
        loadWallpapers(page = 1, query = query, isInitialLoad = true, isSearchWipe = true)
    }

    fun loadNextPage() {
        if (!_uiState.value.isLoading && !_uiState.value.isEndOfList) {
            loadWallpapers(page = _uiState.value.currentPage)
        }
    }

    fun retryInitialLoad() {
        _uiState.update {
            it.copy(
                items = emptyList(),
                currentPage = 1,
                isEndOfList = false,
                isLoading = true,
                error = null
            )
        }
        loadWallpapers(page = 1, query = _uiState.value.currentQuery, isInitialLoad = true)
    }

    fun getImageById(imageId: String): ImageItem? {
        return _uiState.value.items?.find { it.id == imageId }
    }


    companion object {
        fun createFactory(
            sourceId: Int,
            repository: WallpaperRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    val application = checkNotNull(extras[APPLICATION_KEY])
                    return WallpaperSourceListViewModel(sourceId, repository) as T
                }
            }
    }

}