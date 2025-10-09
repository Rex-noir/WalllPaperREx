package com.ace.wallpaperrex.ui.screens.models

import android.annotation.SuppressLint
import android.app.Application
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ace.wallpaperrex.data.daos.getWallpaperSourcesFlow
import com.ace.wallpaperrex.data.daos.setDefaultWallpaperSourceId
import com.ace.wallpaperrex.data.daos.setWallpaperApiKey
import com.ace.wallpaperrex.ui.models.WallpaperSourceItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WallpaperSourceViewModel(application: Application) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    // The ViewModel now has a single source of truth for its state.
    val sources: StateFlow<List<WallpaperSourceItem>> =
        context.getWallpaperSourcesFlow()
            .stateIn(
                scope = viewModelScope,
                // The flow starts when the UI is visible and stops 5s after it's gone.
                // This is efficient and handles configuration changes.
                started = SharingStarted.WhileSubscribed(5000),
                // The initial value before the flow emits its first list.
                initialValue = emptyList()
            )

    fun setAsDefault(id: Int) {
        viewModelScope.launch {
            context.setDefaultWallpaperSourceId(id)
        }
    }

    fun updateWallpaperApiKey(apiKeyDatastoreKey: Preferences.Key<String>, apiKey: String) {
        viewModelScope.launch {
            context.setWallpaperApiKey(apiKeyDatastoreKey, apiKey)
        }
    }
}
