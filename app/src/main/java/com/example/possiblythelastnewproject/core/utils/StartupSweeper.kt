package com.example.possiblythelastnewproject.core.utils

import android.content.Context
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StartupSweeper @Inject constructor(
    private val uriCollector: ReferencedUriCollector,
    private val orphanHunter: MediaOrphanHunter
) {

    suspend fun run(context: Context) {
        val referencedUris = uriCollector.collect()
        val report = orphanHunter.clean(context, referencedUris)
        Log.i("StartupSweeper", "🧹 Sweep → Deleted: ${report.deleted.size}, Retained: ${report.skipped.size}")
        // Optional: Show toast, notify overlay, cache last sweep report, etc.
    }
}