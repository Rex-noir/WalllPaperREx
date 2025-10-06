package com.ace.wallpaperrex.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL


object ImageFileHelper {

    /**
     * Downloads the raw bytes of an image from a given URL.
     * Returns null if the download fails.
     */
    fun getImageBytesFromUrl(url: String): ByteArray? {
        return try {
            URL(url).readBytes()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Saves raw image bytes to a given Uri.
     * This is a suspend function since it performs I/O on a background thread.
     */
    suspend fun saveRawBytesToUri(context: Context, bytes: ByteArray, destinationUri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(destinationUri)?.use { output ->
                    output.write(bytes)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
