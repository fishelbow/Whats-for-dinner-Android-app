package com.example.possiblythelastnewproject.core.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaOrphanHunter @Inject constructor() {

    data class CleanupReport(
        val deleted: List<String>,
        val skipped: List<String>
    )

    private val internalKeepers = setOf("profileInstalled")

    fun clean(context: Context, referencedUris: Set<Uri>): CleanupReport {
        val referencedFilenames = referencedUris
            .mapNotNull { it.lastPathSegment }
            .toSet()

        val deleted = mutableListOf<String>()
        val skipped = mutableListOf<String>()

        val files = context.filesDir.listFiles()
            ?: return CleanupReport(emptyList(), emptyList())

        for (file in files) {
            val name = file.name

            if (internalKeepers.contains(name)) {
                skipped += name
                Log.i("OrphanHunter", "🛡️ Preserved internal file: $name")
                continue
            }

            if (!referencedFilenames.contains(name)) {
                file.delete()
                Log.i("OrphanHunter", "🗑️ Deleted: $name")
                deleted += name
            } else {
                skipped += name
            }
        }

        return CleanupReport(deleted, skipped)
    }
}