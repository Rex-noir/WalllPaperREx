package com.ace.wallpaperrex.data.models

import androidx.datastore.preferences.core.Preferences
import com.ace.wallpaperrex.ui.models.SourceApi
import com.ace.wallpaperrex.ui.models.SourceResponseMapping

data class WallpaperSourceConfigItem(
    val label: String,
    val uniqueKey:String,
    val apiKey: String? = "",
    val apiKeyDataStoreKey: Preferences.Key<String>,
    val supportApiKey: Boolean = true,
    val requireApiKey: Boolean = false,
    val isDefault: Boolean = false,
    val documentationUrl: String? = null,
    val sourceApi: SourceApi,
    val responseMapping: SourceResponseMapping
) {
    val hasApiKey: Boolean
        get() = !apiKey.isNullOrBlank() && supportApiKey
    val isConfigured: Boolean get() = requireApiKey && hasApiKey || !requireApiKey
}