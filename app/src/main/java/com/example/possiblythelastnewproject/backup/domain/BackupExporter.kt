package com.example.possiblythelastnewproject.backup.domain

import android.content.Context
import android.media.MediaScannerConnection
import com.example.possiblythelastnewproject.backup.di.FullDatabaseBackupFactory
import com.example.possiblythelastnewproject.backup.ui.viewModel.BackupSerializer
import com.example.possiblythelastnewproject.backup.ui.viewModel.FullDatabaseBackup
import com.example.possiblythelastnewproject.core.data.AppDatabase
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

class BackupExporter @Inject constructor(
    private val context: Context,
    private val serializer: BackupSerializer
) {
    suspend fun exportBundle(db: AppDatabase, outputZip: File): Result<File> = runCatching {
        val backup = FullDatabaseBackupFactory.createFrom(db)
        val json = serializer.serialize(backup)
        val imageFiles = collectImageFiles(backup)

        ZipOutputStream(FileOutputStream(outputZip)).use { zipOut ->
            zipOut.putNextEntry(ZipEntry("backup.json"))
            zipOut.write(json.toByteArray())
            zipOut.closeEntry()

            imageFiles.forEach { file ->
                zipOut.putNextEntry(ZipEntry("images/${file.name}"))
                file.inputStream().use { it.copyTo(zipOut) }
                zipOut.closeEntry()
            }
        }

        scanFile(outputZip, mimeType = "application/zip")
        outputZip
    }

    private fun collectImageFiles(backup: FullDatabaseBackup): List<File> {
        val allPaths = backup.recipes.mapNotNull { it.imageUri } +
                backup.pantryItems.mapNotNull { it.imageUri }
        return allPaths.map { File(context.filesDir, it) }.filter { it.exists() }
    }

    private fun scanFile(file: File, mimeType: String) {
        MediaScannerConnection.scanFile(
            context, arrayOf(file.absolutePath), arrayOf(mimeType), null
        )
    }
}