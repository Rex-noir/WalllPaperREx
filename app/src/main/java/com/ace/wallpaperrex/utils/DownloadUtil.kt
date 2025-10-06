package com.ace.wallpaperrex.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL


fun getImageBytesFromUrl(url: String): ByteArray? {
    return try {
        URL(url).readBytes()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun saveRawBytesToUri(context: Context, bytes: ByteArray, destinationUri: Uri) {
    withContext(Dispatchers.IO) {
        context.contentResolver.openOutputStream(destinationUri)?.use { output ->
            output.write(bytes)
        }
    }
}
