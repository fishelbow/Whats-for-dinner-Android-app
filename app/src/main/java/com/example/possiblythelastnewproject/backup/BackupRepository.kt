package com.example.possiblythelastnewproject.backup

import android.net.Uri
import javax.inject.Inject

class ZipBackupRepository @Inject constructor(
    private val importer: ZipImporter,
    private val exporter: ZipExporter
) {
    suspend fun exportDatabaseToZip(
        uri: Uri,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): String {
        return exporter.export(uri, onProgress)
    }

    suspend fun importZipToDatabase(
        uri: Uri,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): String {
        return importer.import(uri, onProgress)
    }
}