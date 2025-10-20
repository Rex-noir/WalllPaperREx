package com.ace.wallpaperrex.data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ace.wallpaperrex.data.daos.FavoriteImageDao
import com.ace.wallpaperrex.data.daos.FavoriteImageDao_Impl
import com.ace.wallpaperrex.data.database.AppDatabase
import com.ace.wallpaperrex.data.repositories.DataStoreRepository
import com.ace.wallpaperrex.data.repositories.FavoriteImageRepository
import com.ace.wallpaperrex.data.repositories.GeneralSettingsRepository
import com.ace.wallpaperrex.data.repositories.SourcesRepositoryImpl
import com.ace.wallpaperrex.data.repositories.WallpaperSourceRepository
import com.ace.wallpaperrex.ui.screens.models.AutoChangeWallpaperSetting
import kotlinx.coroutines.flow.first

class WallpaperChangeWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val dataStoreRepository = DataStoreRepository(context = applicationContext)
    private val sourceRepository = SourcesRepositoryImpl(context = applicationContext)
    private val wallpaperSourceRepository = WallpaperSourceRepository(
        sourceRepository = sourceRepository,
        dataStoreRepository = dataStoreRepository
    )
    private val generalSettingsRepository = GeneralSettingsRepository(
        context = context,
        dataStoreRepository = dataStoreRepository,
        wallpaperSourceRepository = wallpaperSourceRepository
    )
    private val favoriteImageRepository =
        FavoriteImageRepository(dao = AppDatabase.getDatabase(context).favoriteImageDao())

    override suspend fun doWork(): Result {
        try {
            val latestConfig = generalSettingsRepository.autoChangeWallpaperSetting.first()

            if (!latestConfig.enabled) return Result.success()

            if (latestConfig.source == AutoChangeWallpaperSetting.Source.FAVORITES) {
                

            } else {

            }

            // Fetch wallpaper list
            val wallpapers = if (latestConfig.customSources.isNotEmpty()) {
                sourceRepository.getWallpapersByKeys(latestConfig.customSources)
            } else {
                sourceRepository.getFavoriteWallpapers()
            }

            if (wallpapers.isEmpty()) return Result.failure()

            // Pick and set a wallpaper
            sourceRepository.setWallpaper(wallpapers.random())

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
}
