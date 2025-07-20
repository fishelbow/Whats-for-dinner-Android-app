package com.example.possiblythelastnewproject.core.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri

fun sweepUnusedImages(
    context: Context,
    referencedImageUris: Set<String>,
    logger: (String) -> Unit = { Log.d("ImageSweep", it) }
) {
    val filesDir = context.filesDir
    val allFiles = filesDir.listFiles()?.toList().orEmpty()

    val filesToDelete = allFiles.filter { file ->
        val fullUri = "content://${context.packageName}.provider/images/${file.name}"
        fullUri !in referencedImageUris
    }

    filesToDelete.forEach { file ->
        val uri = "content://${context.packageName}.provider/images/${file.name}".toUri()
        val deleted = context.contentResolver.delete(uri, null, null)
        logger("🧹 Deleted orphan image → ${file.name} | success: $deleted")
    }

    logger("✅ Sweep complete. ${filesToDelete.size} files removed.")
}