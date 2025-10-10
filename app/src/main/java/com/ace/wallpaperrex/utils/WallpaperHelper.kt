package com.ace.wallpaperrex.utils

import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
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

    fun calculateAverageColor(hexColors: List<String>): Color? {
        if (hexColors.isEmpty()) {
            return null
        }

        var totalRed = 0f
        var totalGreen = 0f
        var totalBlue = 0f

        hexColors.forEach { hexString ->
            try {
                val colorInt = hexString.toColorInt()
                totalRed += android.graphics.Color.red(colorInt)
                totalGreen += android.graphics.Color.green(colorInt)
                totalBlue += android.graphics.Color.blue(colorInt)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }

        val colorCount = hexColors.size
        val averageRed = (totalRed / colorCount).toInt()
        val averageGreen = (totalGreen / colorCount).toInt()
        val averageBlue = (totalBlue / colorCount).toInt()
        return Color(averageRed, averageGreen, averageBlue)
    }

    fun createPlaceholderColor(
        averageColor: Color,
        backgroundColor: Color,
        blendRatio: Float = 0.2f
    ): Color {
        val blended = ColorUtils.blendARGB(
            backgroundColor.hashCode(),
            averageColor.hashCode(),
            blendRatio
        )

        return Color(blended)
    }

    object ColorSerializer : KSerializer<Color> {
        override val descriptor: SerialDescriptor
            get() = PrimitiveSerialDescriptor("Color", PrimitiveKind.INT)

        override fun serialize(
            encoder: Encoder,
            value: Color
        ) {
            encoder.encodeInt(value.toArgb())
        }

        override fun deserialize(decoder: Decoder): Color {
            return Color(decoder.decodeInt())
        }

    }
}
