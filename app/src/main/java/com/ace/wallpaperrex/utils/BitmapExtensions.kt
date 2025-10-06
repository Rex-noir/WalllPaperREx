package com.ace.wallpaperrex.utils

import android.graphics.Bitmap
import android.os.Build
import java.io.ByteArrayOutputStream

fun Bitmap.convertToByteArray(): ByteArray {
    val outputStream = ByteArrayOutputStream()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, outputStream)
    } else {
        compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    }
    return outputStream.toByteArray()
}
