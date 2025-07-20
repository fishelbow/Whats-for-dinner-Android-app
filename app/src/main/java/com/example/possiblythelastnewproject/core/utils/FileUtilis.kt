package com.example.possiblythelastnewproject.core.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import androidx.core.net.toUri

fun deleteImageFromStorage(uriStr: String, context: Context): Boolean {
    val tag = "ImageCleanup"

    if (uriStr.isBlank()) {
        Log.w(tag, "⚠️ Skipping deletion — blank URI string")
        return false
    }

    Log.d(tag, "🧹 Deletion initiated → $uriStr")

    return runCatching {
        val uri = uriStr.toUri()
        Log.d(tag, "🔗 Parsed URI → $uri | Scheme: ${uri.scheme}")

        when (uri.scheme) {
            "file" -> {
                val file = uri.path?.let { File(it) }
                if (file == null || !file.exists()) {
                    Log.w(tag, "🚫 File not found → ${file?.absolutePath}")
                    return false
                }
                val deleted = file.delete()
                if (deleted) {
                    Log.d(tag, "✅ Deleted image → ${file.name}")
                } else {
                    Log.w(tag, "❌ File deletion failed → ${file.name}")
                }
                deleted
            }

            "content" -> {
                val deletedRows = context.contentResolver.delete(uri, null, null)
                Log.d(tag, "🧨 contentResolver.delete result → $deletedRows row(s) affected")
                deletedRows > 0
            }

            else -> {
                val fileName = uri.lastPathSegment ?: run {
                    Log.w(tag, "🚫 Missing lastPathSegment → $uri")
                    return false
                }
                val file = File(context.filesDir, fileName)
                Log.d(tag, "📁 Resolved fallback file path → ${file.absolutePath}")

                if (!file.exists()) {
                    Log.w(tag, "🚫 File not found → ${file.absolutePath}")
                    return false
                }

                val deleted = file.delete()
                if (deleted) {
                    Log.d(tag, "✅ Deleted image → ${file.name}")
                } else {
                    Log.w(tag, "❌ File deletion failed → ${file.name}")
                }
                deleted
            }
        }
    }.getOrElse { e ->
        Log.e(tag, "🔥 Exception during deletion → ${e.message}", e)
        false
    }
}