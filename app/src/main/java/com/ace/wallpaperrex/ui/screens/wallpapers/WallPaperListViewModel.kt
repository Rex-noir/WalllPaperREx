package com.ace.wallpaperrex.ui.screens.wallpapers

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ace.wallpaperrex.data.repositories.WallhavenImageRepository
import com.ace.wallpaperrex.data.repositories.WallhavenImageRepositoryImpl
import com.ace.wallpaperrex.data.models.toImageItem
import com.ace.wallpaperrex.ui.models.ImageItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class WallpaperListUiState(
    val items: List<ImageItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val isEndOfList: Boolean = false,
    val currentQuery: String = "nature"
)

class WallPaperListViewModel(private val wallhavenImageRepository: WallhavenImageRepository) :
    ViewModel() {

    private val _uiState = MutableStateFlow(WallpaperListUiState())
    val uiState: StateFlow<WallpaperListUiState> = _uiState.asStateFlow();

    init {
        loadWallpapers(page = 1, query = _uiState.value.currentQuery, isInitialLoad = true);
    }

    fun loadWallpapers(
        page: Int,
        query: String? = "nature",
        isInitialLoad: Boolean = false,
        isSearchWipe: Boolean = false
    ) {

        if (_uiState.value.isLoading || (_uiState.value.isEndOfList && !isInitialLoad && isSearchWipe)) {
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
            val result = wallhavenImageRepository.getImages(page = page, query = query);
            result.fold(onSuccess = { imageResponse ->
                val newUiItems = imageResponse.data.map { detail -> detail.toImageItem() }

                _uiState.update { currentState ->
                    val combinedItems = if (isInitialLoad || isSearchWipe) {
                        newUiItems
                    } else {
                        currentState.items + newUiItems
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
                        error = error.localizedMessage ?: "An unknown error occured"
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
                error = null
            )
        }
        loadWallpapers(page = 1, query = _uiState.value.currentQuery, isInitialLoad = true)
    }

    fun getImageById(imageId: String): ImageItem? {
        return _uiState.value.items.find { it.id == imageId }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = WallhavenImageRepositoryImpl()
                return WallPaperListViewModel(repository) as T
            }
        }
    }
}