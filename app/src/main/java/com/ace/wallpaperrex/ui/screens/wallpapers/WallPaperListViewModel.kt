package com.ace.wallpaperrex.ui.screens.wallpapers

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ace.wallpaperrex.data.daos.getLastWallpaperSource
import com.ace.wallpaperrex.data.repositories.WallpaperRepository
import com.ace.wallpaperrex.data.repositories.WallpaperRepositoryProvider
import com.ace.wallpaperrex.ui.models.ImageItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
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

class WallPaperListViewModel(application: Application) :
    AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(WallpaperListUiState())
    private lateinit var repository: WallpaperRepository
    val uiState: StateFlow<WallpaperListUiState> = _uiState.asStateFlow();

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            application.applicationContext.getLastWallpaperSource().filterNotNull()
                .distinctUntilChanged().collect { lastSource ->
                    repository = WallpaperRepositoryProvider.provide(lastSource)
                    _uiState.update {
                        it.copy(
                            items = emptyList(),
                        )
                    }
                    loadWallpapers(page = 1, isInitialLoad = true, isSearchWipe = true)
                }
        }
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
        return _uiState.value.items.find { it.id == imageId }
    }
}