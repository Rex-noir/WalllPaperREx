package com.ace.wallpaperrex.ui.screens.models

import android.annotation.SuppressLint
import android.app.Application
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ace.wallpaperrex.data.daos.getWallpaperSourcesFlow
import com.ace.wallpaperrex.data.daos.setDefaultWallpaperSourceId
import com.ace.wallpaperrex.data.daos.setWallpaperApiKey
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SourcesSettingsViewModel(application: Application) : AndroidViewModel(application) {
    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    val sources = context.getWallpaperSourcesFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setAsDefault(id: Int) {
        viewModelScope.launch {
            context.setDefaultWallpaperSourceId(id)
        }
    }

    fun updateSourceApiKey(apiKeyDatastoreKey: Preferences.Key<String>, apiKey: String) {
        viewModelScope.launch {
            context.setWallpaperApiKey(apiKeyDatastoreKey, apiKey)
        }
    }
}