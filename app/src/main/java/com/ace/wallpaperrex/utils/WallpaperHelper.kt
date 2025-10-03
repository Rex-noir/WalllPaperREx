package com.ace.wallpaperrex.utils

import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

object WallpaperHelper {
    enum class ScreenTarget {
        HOME, LOCK, BOTH
    }

    suspend fun applyWallpaperFromUrl(context: Context, url: String, target: ScreenTarget) {
        withContext(Dispatchers.IO) @androidx.annotation.RequiresPermission(android.Manifest.permission.SET_WALLPAPER) {
            val wallpaperManager = WallpaperManager.getInstance(context)
            val bitmap = URL(url).openStream().use { stream ->
                BitmapFactory.decodeStream(stream)
            } ?: return@withContext

            val flag = when (target) {
                ScreenTarget.BOTH -> WallpaperManager.FLAG_LOCK or WallpaperManager.FLAG_SYSTEM
                ScreenTarget.HOME -> WallpaperManager.FLAG_SYSTEM
                ScreenTarget.LOCK -> WallpaperManager.FLAG_LOCK
            }

            wallpaperManager.setBitmap(bitmap, null, true, flag);
        }
    }
}