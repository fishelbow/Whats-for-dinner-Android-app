package com.example.possiblythelastnewproject.core.utils

import android.content.Context
import android.util.Log
import java.io.File
import androidx.core.net.toUri
import java.io.IOException

fun deleteImageFromStorage(uriStr: String, context: Context): Boolean {
    return runCatching {
        val uri = uriStr.toUri()

        val file = when (uri.scheme) {
            "file" -> File(uri.path ?: throw IOException("Missing path for file URI"))
            else -> {
                val fileName = uri.lastPathSegment ?: throw IOException("Missing file name from URI")
                File(context.filesDir, fileName)
            }
        }

        if (!file.exists()) {
            Log.w("ImageCleanup", "File does not exist → ${file.absolutePath}")
            return false
        }

        val deleted = file.delete()
        if (deleted) {
            Log.d("ImageCleanup", "Deleted image → ${file.absolutePath}")
        } else {
            Log.w("ImageCleanup", "Failed to delete image → ${file.absolutePath}")
        }
        deleted
    }.getOrElse { e ->
        Log.e("ImageCleanup", "Exception deleting image: ${e.message}", e)
        false
    }
}