package com.example.possiblythelastnewproject.backup

import android.net.Uri
import javax.inject.Inject

class ZipBackupRepository @Inject constructor(
    private val importer: ZipImporter,
    private val exporter: ZipExporter
) {
    suspend fun exportDatabaseToZip(uri: Uri): String {
        return exporter.export(uri)
    }

    suspend fun importZipToDatabase(uri: Uri): String {
        return importer.import(uri)
    }
}