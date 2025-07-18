package com.example.possiblythelastnewproject.core.utils

import android.content.Context
import android.util.Log
import java.io.File
import androidx.core.net.toUri

fun deleteImageFromStorage(uriStr: String, context: Context): Boolean {
    return try {
        val uri = uriStr.toUri()

        // Extract a path relative to app storage
        val file = if (uri.scheme == "file") {
            File(uri.path!!) // Already local
        } else {
            val fileName = uri.lastPathSegment ?: return false
            File(context.filesDir, fileName)
        }

        val deleted = file.exists() && file.delete()
        Log.d("ImageCleanup", "Resolved path: ${file.absolutePath}, deleted: $deleted")
        deleted
    } catch (e: Exception) {
        Log.w("ImageCleanup", "Failed to delete image: ${e.message}")
        false
    }
}