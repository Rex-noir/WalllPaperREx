package com.ace.wallpaperrex.data.repositories

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ace.wallpaperrex.data.workers.WallpaperChangeWorker
import com.ace.wallpaperrex.ui.screens.models.AutoChangeWallpaperSetting
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

interface AutoChangeWallpaperSettingMethods {
    suspend fun updateAutoChangeWallpaperEnabled(enabled: Boolean): Unit
    suspend fun updateAutoChangeWallpaperInterval(interval: Int): Unit
    suspend fun updateAutoChangeWallpaperSource(source: String): Unit
    suspend fun updateAutoChangeWallpaperCustomSources(sources: String): Unit
}

object GeneralSettingsConstants {
    const val AUTO_CHANGE_WORK_MANAGER_TAG = "auto_change_wallpaper"
}

@Singleton
class GeneralSettingsRepository @Inject constructor(
    @ApplicationContext val context: Context,
    val dataStoreRepository: DataStoreRepository,
    val wallpaperSourceRepository: WallpaperSourceRepository,
) : AutoChangeWallpaperSettingMethods {

    val autoChangeWallpaperSetting: Flow<AutoChangeWallpaperSetting> = combine(
        wallpaperSourceRepository.wallpaperSources,
        dataStoreRepository.autoChangeWallpaperDataStore
    ) { wallpaperSources, prefs ->
        val customSources =
            prefs[DataStoreKeys.AUTO_CHANGE_WALLPAPER_CUSTOM_SOURCES]?.split(",")
                ?.mapNotNull { customSourceKey ->
                    wallpaperSources.find { it.uniqueKey == customSourceKey }
                }

        val isEnabled = prefs[DataStoreKeys.AUTO_CHANGE_WALLPAPER_ENABLED_KEY] ?: false
        val interval = prefs[DataStoreKeys.AUTO_CHANGE_WALLPAPER_INTERVAL_KEY] ?: 15
        Log.d("GeneralSettingsRepository", "customSources $customSources");
        Log.d(
            "Raw Custom Sources",
            prefs[DataStoreKeys.AUTO_CHANGE_WALLPAPER_CUSTOM_SOURCES].toString()
        )
        Log.d("WallpaperSources", wallpaperSources.toString())
        val sourceParsed =
            AutoChangeWallpaperSetting.Source.fromKey(prefs[DataStoreKeys.AUTO_CHANGE_WALLPAPER_SOURCE])
        val source =
            if (customSources?.isNotEmpty() == true && sourceParsed != null) {
                sourceParsed
            } else {
                AutoChangeWallpaperSetting.Source.FAVORITES
            }
        AutoChangeWallpaperSetting(
            enabled = isEnabled,
            interval = interval,
            source = source,
            customSources = customSources ?: emptyList()
        )
    }

    suspend fun observeSettingsForWorker(scope: CoroutineScope) {
        scope.launch {
            autoChangeWallpaperSetting
                .distinctUntilChanged() // Only emit when settings actually change
                .collect { settings ->
                    val workManager = WorkManager.getInstance(context)
                    if (settings.enabled) {
                        val workRequest = PeriodicWorkRequestBuilder<WallpaperChangeWorker>(
                            repeatInterval = settings.interval.toLong().coerceAtLeast(15),
                            TimeUnit.MINUTES
                        ).build()

                        workManager.enqueueUniquePeriodicWork(
                            GeneralSettingsConstants.AUTO_CHANGE_WORK_MANAGER_TAG,
                            ExistingPeriodicWorkPolicy.UPDATE,
                            workRequest
                        )
                    } else {
                        workManager.cancelUniqueWork(GeneralSettingsConstants.AUTO_CHANGE_WORK_MANAGER_TAG)
                    }
                }
        }
    }

    override suspend fun updateAutoChangeWallpaperEnabled(enabled: Boolean) {
        dataStoreRepository.updateAutoChangeWallpaperEnabled(enabled)
    }

    override suspend fun updateAutoChangeWallpaperInterval(interval: Int) {
        dataStoreRepository.updateAutoChangeWallpaperInterval(interval)
    }

    override suspend fun updateAutoChangeWallpaperSource(source: String) {
        dataStoreRepository.updateAutoChangeWallpaperSource(source)
    }

    override suspend fun updateAutoChangeWallpaperCustomSources(sources: String) {
        dataStoreRepository.updateAutoChangeWallpaperCustomSources(sources)
    }
}
