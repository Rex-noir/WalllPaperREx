package com.ace.wallpaperrex.data.repositories

import android.content.Context
import android.net.Uri
import com.ace.wallpaperrex.data.models.WallpaperSourceConfig
import com.ace.wallpaperrex.utils.mapToUserFriendlyException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okio.IOException
import java.io.File
import java.net.URL

interface SourcesRepository {
    val sourcesConfig: StateFlow<Result<WallpaperSourceConfig>>

    suspend fun triggerInitialLoadI()
    suspend fun updateFromNetwork(url: String): Result<Unit>
    suspend fun importFromFile(uri: Uri): Result<Unit>
}


class SourcesRepositoryImpl(
    private val context: Context
) : SourcesRepository {
    private val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }
    private val _sourcesConfig = MutableStateFlow<Result<WallpaperSourceConfig>>(
        Result.failure(
            Exception("Not initialized")
        )
    )
    override val sourcesConfig = _sourcesConfig.asStateFlow()

    override suspend fun triggerInitialLoadI() {
        if (_sourcesConfig.value.isSuccess) return
        _sourcesConfig.value = loadConfigFromStorage()
    }

    companion object {
        private const val CURRENT_SOURCES_FILENAME = "wallpaper_sources.json"
    }

    private suspend fun loadConfigFromStorage(): Result<WallpaperSourceConfig> =
        withContext(Dispatchers.IO) {
            try {
                val internalConfig = File(context.filesDir, CURRENT_SOURCES_FILENAME)
                val jsonString = if (internalConfig.exists()) {
                    internalConfig.readText()
                } else {
                    val assetsJson =
                        context.assets.open("sources.json").bufferedReader().use { it.readText() }
                    internalConfig.writeText(assetsJson)
                    assetsJson
                }
                Result.success(jsonParser.decodeFromString<WallpaperSourceConfig>(jsonString))
            } catch (e: Exception) {
                Result.failure(mapToUserFriendlyException(e))
            }
        }

    override suspend fun updateFromNetwork(url: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val newJsonString = URL(url).readText()
                val newConfig = jsonParser.decodeFromString<WallpaperSourceConfig>(newJsonString)
                saveConfigAndEmit(newJsonString, newConfig)
            }.onFailure {
                return@withContext Result.failure(mapToUserFriendlyException(it))
            }
            Result.success(Unit)
        }

    private fun saveConfigAndEmit(jsonString: String, newConfig: WallpaperSourceConfig) {
        val internalConfigFile = File(context.filesDir, CURRENT_SOURCES_FILENAME)
        internalConfigFile.writeText(jsonString)
        _sourcesConfig.value = Result.success(newConfig)
    }

    override suspend fun importFromFile(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val newJsonString = context.contentResolver.openInputStream(uri)?.bufferedReader()
                .use { it?.readText() } ?: throw IOException("Could not read from file URI")
            val newConfig = jsonParser.decodeFromString<WallpaperSourceConfig>(newJsonString)
            saveConfigAndEmit(newJsonString, newConfig)
        }.onFailure {
            return@withContext Result.failure(mapToUserFriendlyException(it))
        }
        Result.success(Unit)
    }
}