package com.ace.wallpaperrex.data.repositories

import android.content.Context
import android.net.Uri
import com.ace.wallpaperrex.ui.models.WallpaperSourceConfigItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.net.URL

interface SourcesRepository {
    /**
     * Gets the current sources configuration. It will first try to load from
     * internal storage, and fall back to the bundled assets file if not found.
     */
    suspend fun getSourcesConfig(): Result<WallpaperSourceConfig>

    /**
     * Fetches a new configuration from a given URL and replaces the
     * current one in internal storage.
     * @return A Result indicating success or failure.
     */
    suspend fun updateFromNetwork(url: String): Result<Unit>

    /**
     * Imports a configuration from a user-selected file URI and replaces
     * the current one in internal storage.
     * @return A Result indicating success or failure.
     */
    suspend fun importFromFile(uri: Uri): Result<Unit>
}

@Serializable
data class WallpaperSourceConfig(
    val sources: List<WallpaperSourceConfigItem>
)

class SourcesRepositoryImpl(
    private val context: Context
) : SourcesRepository {
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private var cachedConfig: WallpaperSourceConfig? = null

    companion object {
        private const val CURRENT_SOURCES_FILENAME = "wallpaper_sources.json"
    }

    override suspend fun getSourcesConfig(): Result<WallpaperSourceConfig> {
        cachedConfig?.let withContext@{ return@withContext Result.success(it) }
        return try {
            val internalConfigFile = File(context.filesDir, CURRENT_SOURCES_FILENAME)
            val config = if (internalConfigFile.exists()) {
                val jsonString = internalConfigFile.readText()
                jsonParser.decodeFromString<WallpaperSourceConfig>(jsonString)
            } else {
                val jsonString =
                    context.assets.open("sources.json").bufferedReader().use { it.readText() }
                internalConfigFile.writeText(jsonString)
                jsonParser.decodeFromString<WallpaperSourceConfig>(jsonString)
            }
            cachedConfig = config
            Result.success(config)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateFromNetwork(url: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                // In a real app, use a proper networking client like Retrofit or Ktor
                val newJsonString = URL(url).readText()
                // Validate the JSON before saving
                jsonParser.decodeFromString<WallpaperSourceConfig>(newJsonString)

                saveConfig(newJsonString)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun importFromFile(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val newJsonString =
                context.contentResolver.openInputStream(uri)?.bufferedReader()
                    .use { it?.readText() }
            if (newJsonString.isNullOrBlank()) {
                return@withContext Result.failure(Exception("Selected file is empty or could not be read."))
            }
            // Validate the JSON before saving
            jsonParser.decodeFromString<WallpaperSourceConfig>(newJsonString)

            saveConfig(newJsonString)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun saveConfig(jsonString: String) {
        val internalConfigFile = File(context.filesDir, CURRENT_SOURCES_FILENAME)
        internalConfigFile.writeText(jsonString)
        // Invalidate the cache so the next `get` call reads the new file
        cachedConfig = null
    }
}