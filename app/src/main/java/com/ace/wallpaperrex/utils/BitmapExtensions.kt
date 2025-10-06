package com.ace.wallpaperrex.utils

import android.graphics.Bitmap
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

suspend fun Bitmap.convertToWebpBytes(): ByteArray = withContext(Dispatchers.IO) {
    val outputStream = ByteArrayOutputStream()
    val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Bitmap.CompressFormat.WEBP_LOSSLESS
    } else {
        Bitmap.CompressFormat.WEBP
    }
    compress(format, 100, outputStream)
    outputStream.toByteArray()
}
