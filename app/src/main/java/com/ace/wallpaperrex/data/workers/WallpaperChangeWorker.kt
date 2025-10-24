package com.ace.wallpaperrex.data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ace.wallpaperrex.data.database.AppDatabase
import com.ace.wallpaperrex.data.repositories.DataStoreRepository
import com.ace.wallpaperrex.data.repositories.FavoriteImageRepository
import com.ace.wallpaperrex.data.repositories.GeneralSettingsRepository
import com.ace.wallpaperrex.data.repositories.SourcesRepositoryImpl
import com.ace.wallpaperrex.data.repositories.WallpaperRepositoryImpl
import com.ace.wallpaperrex.data.repositories.WallpaperSourceRepository
import com.ace.wallpaperrex.ui.screens.models.AutoChangeWallpaperSetting
import com.ace.wallpaperrex.utils.ImageFileHelper
import com.ace.wallpaperrex.utils.WallpaperHelper
import kotlinx.coroutines.flow.first
import java.io.File

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

            val target = WallpaperHelper.ScreenTarget.BOTH;
            if (!latestConfig.enabled) return Result.success()

            if (latestConfig.source == AutoChangeWallpaperSetting.Source.FAVORITES) {
                val favoriteImages = favoriteImageRepository.getAllFavorites().first()
                if (favoriteImages.isNotEmpty()) {
                    val image = favoriteImages.random()
                    if (image.localPath != null) {
                        val file = File(image.localPath)
                        if (file.exists()) {
                            WallpaperHelper.applyWallpaper(
                                context = applicationContext,
                                rawBytes = file.readBytes(),
                                target = target
                            )
                        } else {
                            // Fallback to URL if local file is missing
                            WallpaperHelper.applyWallpaper(
                                context = applicationContext,
                                url = image.url,
                                target = target
                            )
                        }
                    } else {
                        WallpaperHelper.applyWallpaper(
                            context = applicationContext,
                            url = image.url,
                            target = target
                        )
                    }
                }
            } else {
                val sources =
                    wallpaperSourceRepository.wallpaperSources.first().filter { it.isConfigured }
                if (sources.isNotEmpty()) {
                    val source = sources.random();
                    val wallpaperRepository = WallpaperRepositoryImpl(
                        source = source
                    )
                    val result = wallpaperRepository.getRandomWallpaper();
                    if (result.isSuccess) {
                        val image = result.getOrNull()
                        if (image != null) {
                            val bytes = ImageFileHelper.getImageBytesFromUrl(image.url);
                            if (bytes != null) {
                                WallpaperHelper.applyWallpaper(
                                    context = applicationContext,
                                    rawBytes = bytes,
                                    target = target
                                );
                            }

                        }
                    } else {
                        return Result.retry()
                    }
                }
            }

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
}
