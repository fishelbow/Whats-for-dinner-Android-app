package com.example.possiblythelastnewproject.core.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri

object MediaOrphanHunter {
    fun clean(context: Context, referencedUris: Set<String>): CleanupReport {
        val referencedFilenames = referencedUris
            .mapNotNull { it.toUri().lastPathSegment }
            .toSet()

        val deleted = mutableListOf<String>()
        val skipped = mutableListOf<String>()

        val files = context.filesDir.listFiles() ?: return CleanupReport(emptyList(), emptyList())
        for (file in files) {
            val name = file.name
            if (!referencedFilenames.contains(name)) {
                file.delete()
                Log.i("OrphanHunter", "Deleted: $name")
                deleted += name
            } else {
                skipped += name
            }
        }

        return CleanupReport(deleted, skipped)
    }

    data class CleanupReport(val deleted: List<String>, val skipped: List<String>)
}