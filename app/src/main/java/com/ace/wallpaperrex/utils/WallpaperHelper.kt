package com.ace.wallpaperrex.utils

import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.net.URL

object WallpaperHelper {
    enum class ScreenTarget {
        HOME, LOCK, BOTH
    }

    suspend fun applyWallpaper(
        context: Context,
        url: String? = null,
        rawBytes: ByteArray? = null,
        target: ScreenTarget
    ) {
        withContext(Dispatchers.IO) @androidx.annotation.RequiresPermission(android.Manifest.permission.SET_WALLPAPER) {
            val wallpaperManager = WallpaperManager.getInstance(context)
            val flag = when (target) {
                ScreenTarget.BOTH -> WallpaperManager.FLAG_LOCK or WallpaperManager.FLAG_SYSTEM
                ScreenTarget.HOME -> WallpaperManager.FLAG_SYSTEM
                ScreenTarget.LOCK -> WallpaperManager.FLAG_LOCK
            }

            if (rawBytes != null) {
                val inputStream = ByteArrayInputStream(rawBytes)
                wallpaperManager.setStream(inputStream, null, true, flag)
            } else if (url != null) {
                // Fallback: download from URL and decode bitmap
                val bitmap = URL(url).openStream().use { stream ->
                    BitmapFactory.decodeStream(stream)
                } ?: return@withContext
                wallpaperManager.setBitmap(bitmap, null, true, flag)
            }
        }
    }
}
