package com.ace.wallpaperrex.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

suspend fun downloadImageRaw(context: Context, imageUrl: String, destinationUri: Uri) {
    withContext(Dispatchers.IO) {
        try {
            val inputStream = URL(imageUrl).openStream()

            context.contentResolver.openOutputStream(destinationUri)
                ?.use { outputStream -> inputStream.copyTo(outputStream) }
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}