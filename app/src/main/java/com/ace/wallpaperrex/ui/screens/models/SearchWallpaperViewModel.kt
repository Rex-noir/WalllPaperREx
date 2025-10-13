package com.ace.wallpaperrex.ui.screens.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.ace.wallpaperrex.data.database.AppDatabase
import com.ace.wallpaperrex.data.entities.SearchHistoryItem
import com.ace.wallpaperrex.data.models.WallpaperSourceConfigItem
import com.ace.wallpaperrex.data.repositories.WallpaperRepository
import com.ace.wallpaperrex.data.repositories.WallpaperRepositoryImpl
import com.ace.wallpaperrex.data.repositories.WallpaperSourceRepository
import com.ace.wallpaperrex.ui.models.ImageItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchWallpaperViewModel(
    wallpaperSourceRepository: WallpaperSourceRepository,
    application: Application
) : AndroidViewModel(application) {
    private val searchHistoryDao = AppDatabase.getDatabase(application).searchHistoryDao()

    val searchHistory = searchHistoryDao.getSearchHistory().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedSource = MutableStateFlow<WallpaperSourceConfigItem?>(null);
    val selectedSource = _selectedSource.asStateFlow()

    private lateinit var repository: WallpaperRepository
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _images = MutableStateFlow<List<ImageItem>>(emptyList())
    val images = _images.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isEndOfList = MutableStateFlow(false)
    val isEndOfList = _isEndOfList.asStateFlow()

    private val _page = MutableStateFlow(1)
    val page = _page.asStateFlow()

    init {
        viewModelScope.launch {
            _selectedSource.update {
                wallpaperSourceRepository.lastWallpaperSource.first()
            }
            repository = WallpaperRepositoryImpl(_selectedSource.value!!)
        }
    }

    fun addSearchQuery(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            searchHistoryDao.insert(SearchHistoryItem(query = query.trim()))
        }
    }

    fun deleteSearchItem(item: SearchHistoryItem) {
        viewModelScope.launch {
            searchHistoryDao.deleteById(item.id)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            searchHistoryDao.clearAll()
        }
    }

    /**
     * Called when the user submits a new search query from the search bar.
     * This is the primary entry point for a NEW search.
     */
    fun performSearch(query: String) {
        // A new search always starts from page 1 and clears old results.
        resetSearch()
        _searchQuery.update { query }

        // Add to history and then fetch the first page of data.
        addSearchQuery(query)
        fetchNextPage()
    }

    /**
     * Called when the user changes the source filter (e.g., clicks a FilterChip).
     * This triggers a new search for the current query on the new source.
     */
    fun setSelectedSource(source: WallpaperSourceConfigItem) {
        _selectedSource.update { source }

        // A source change also requires a full reset.
        resetSearch()

        viewModelScope.launch {
            // Re-create the repository for the new source.
            repository = WallpaperRepositoryImpl(source)

            // If there's an active query, re-run the search on the new source.
            // If not, the screen will just be empty, which is correct.
            if (searchQuery.value.isNotBlank()) {
                fetchNextPage()
            }
        }
    }

    /**
     * Called when the user scrolls to the end of the list to load more results.
     * This function's only job is to increment the page and fetch data.
     */
    fun loadMore() {
        if (_isLoading.value || _isEndOfList.value) return
        _page.update { it + 1 }
        fetchNextPage()
    }

    /**
     * A private helper function to reset all state for a new search.
     */
    private fun resetSearch() {
        _page.update { 1 }
        _images.update { emptyList() }
        _isEndOfList.update { false }
        _error.update { null }
    }

    /**
     * The single, internal function responsible for making the network call.
     * It uses the current state of `searchQuery` and `page`.
     */
    private fun fetchNextPage() {
        if (_isLoading.value || searchQuery.value.isBlank()) return

        viewModelScope.launch {
            _isLoading.update { true }
            _error.update { null }

            val result = repository.searchImages(page.value, searchQuery.value)

            result.fold(
                onSuccess = { response ->
                    val fetchedImages = response.data
                    // Always append the new images to the existing list.
                    // resetSearch() handles clearing the list when needed.
                    _images.update { it + fetchedImages }

                    _isEndOfList.update {
                        response.meta?.currentPage == response.meta?.lastPage || fetchedImages.isEmpty()
                    }
                },
                onFailure = { error ->
                    _error.value = error.localizedMessage
                }
            )
            _isLoading.update { false }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application =
                    checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                return SearchWallpaperViewModel() as T
            }
        }

        fun createFactory(sourceRepository: WallpaperSourceRepository) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SearchWallpaperViewModel(
                        sourceRepository,
                        application = Application()
                    ) as T
                }
            }
    }
}