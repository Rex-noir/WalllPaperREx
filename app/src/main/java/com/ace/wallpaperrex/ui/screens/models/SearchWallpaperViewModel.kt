package com.ace.wallpaperrex.ui.screens.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.ace.wallpaperrex.data.daos.getDefaultWallpaperSource
import com.ace.wallpaperrex.data.database.AppDatabase
import com.ace.wallpaperrex.data.entities.SearchHistoryItem
import com.ace.wallpaperrex.ui.models.WallpaperSourceItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchWallpaperViewModel(application: Application) : AndroidViewModel(application) {
    private val searchHistoryDao = AppDatabase.getDatabase(application).searchHistoryDao()

    val searchHistory = searchHistoryDao.getSearchHistory().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedSource = MutableStateFlow<WallpaperSourceItem?>(null);
    val selectedSource = _selectedSource.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            _selectedSource.update {
                application.getDefaultWallpaperSource().first()
            }
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

    fun setSelectedSource(source: WallpaperSourceItem) {
        _selectedSource.update { source }
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
                return SearchWallpaperViewModel(application) as T
            }
        }
    }
}