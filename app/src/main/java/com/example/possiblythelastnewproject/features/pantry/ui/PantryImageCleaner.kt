package com.example.possiblythelastnewproject.features.pantry.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri

object PantryImageCleaner {
    fun cleanUnreferencedImages(context: Context, referencedUris: Set<String>) {
        val referencedFilenames = referencedUris
            .mapNotNull { it.toUri().lastPathSegment }
            .toSet()

        val files = context.filesDir.listFiles() ?: return
        for (file in files) {
            val filename = file.name
            if (!referencedFilenames.contains(filename)) {
                file.delete()
                Log.i("OrphanHunter", "Deleted orphan: $filename")
            }
        }
    }
}