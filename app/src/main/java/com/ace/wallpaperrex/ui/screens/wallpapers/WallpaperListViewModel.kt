package com.ace.wallpaperrex.ui.screens.wallpapers

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.ace.wallpaperrex.data.repositories.WallpaperRepository
import com.ace.wallpaperrex.data.repositories.WallpaperRepositoryImpl
import com.ace.wallpaperrex.data.repositories.WallpaperSourceRepository
import com.ace.wallpaperrex.ui.models.ImageItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class WallpaperListUiState(
    val items: List<ImageItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val currentPage: Int = 1,
    val isEndOfList: Boolean = true,
)

class WallpaperListViewModel(
    val sourceKey: String,
    val wallpaperSourceRepository: WallpaperSourceRepository,
) :
    ViewModel() {

    private val _uiState = MutableStateFlow(WallpaperListUiState())
    val uiState: StateFlow<WallpaperListUiState> = _uiState.asStateFlow()

    private lateinit var repository: WallpaperRepository

    init {
        viewModelScope.launch {
            wallpaperSourceRepository.wallpaperSources.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            ).map { sources -> sources.find { it.uniqueKey == sourceKey } }.filterNotNull()
                .collect {
                    repository = WallpaperRepositoryImpl(it)
                    loadWallpapers(page = 1, isInitialLoad = true)
                }
        }

    }


    fun loadWallpapers(
        page: Int,
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
            )
        }

        viewModelScope.launch {
            val result = repository.getImages(
                page = page
            );
            result.fold(onSuccess = { imageResponse ->
                val newUiItems = imageResponse.data
                var isEnd = newUiItems.isEmpty()
                if (!isEnd && imageResponse.meta?.total != null && imageResponse.meta.total != -1) {
                    isEnd = _uiState.value.currentPage >= imageResponse.meta.total
                }
                _uiState.update { currentState ->
                    val combinedItems = if (isInitialLoad || isSearchWipe) {
                        newUiItems
                    } else {
                        val items = currentState.items.plus(newUiItems)
                        items
                    }
                    currentState.copy(
                        items = combinedItems,
                        isLoading = false,
                        currentPage = imageResponse.meta?.currentPage?.plus(1)
                            ?: (currentState.currentPage + 1),
                        isEndOfList = isEnd
                    )
                }
            }, onFailure = { error ->
                Log.e("WallpaperListViewModel", "loadWallpapers: ${error.localizedMessage}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.localizedMessage ?: "An unknown error occurred"
                    )
                }
            })
        }
    }

    fun loadNextPage() {
        if (!_uiState.value.isLoading && !_uiState.value.isEndOfList) {
            loadWallpapers(page = _uiState.value.currentPage)
        }
    }

    fun retry() {
        if (_uiState.value.currentPage == 1) {
            retryInitialLoad()
        } else {
            loadNextPage()
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
        loadWallpapers(page = 1, isInitialLoad = true)
    }

    fun getImageById(imageId: String): ImageItem? {
        return _uiState.value.items.find { it.id == imageId }
    }


    companion object {
        fun createFactory(
            sourceKey: String,
            wallpaperSourceRepository: WallpaperSourceRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    val application = checkNotNull(extras[APPLICATION_KEY])
                    return WallpaperListViewModel(sourceKey, wallpaperSourceRepository) as T
                }
            }
    }

}