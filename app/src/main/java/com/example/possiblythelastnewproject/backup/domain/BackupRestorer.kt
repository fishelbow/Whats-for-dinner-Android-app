package com.example.possiblythelastnewproject.backup.domain

import android.content.Context
import android.net.Uri
import com.example.possiblythelastnewproject.backup.ui.viewModel.BackupSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.zip.ZipInputStream
import javax.inject.Inject

class BackupRestorer @Inject constructor(
    private val context: Context,
    private val serializer: BackupSerializer,
    private val importer: BackupImporter
) {
    suspend fun restoreBundle(zipUri: Uri): ImportResult = withContext(Dispatchers.IO) {
        val tempDir = File(context.cacheDir, "restoreTemp").apply { mkdirs() }
        extractZipToTemp(zipUri, tempDir)

        restoreImages(File(tempDir, "images"), File(context.filesDir, "images"))
        val jsonText = File(tempDir, "backup.json").readText()
        val backup = serializer.deserialize(jsonText).getOrThrow()
        importer.import(backup)
    }

    private fun extractZipToTemp(uri: Uri, tempDir: File) {
        context.contentResolver.openInputStream(uri)?.use { input ->
            ZipInputStream(input).use { zipIn ->
                var entry = zipIn.nextEntry
                while (entry != null) {
                    val outFile = File(tempDir, entry.name).apply { parentFile?.mkdirs() }
                    if (!entry.isDirectory) {
                        outFile.outputStream().use { zipIn.copyTo(it) }
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }
        } ?: throw IOException("Could not open zip input stream")
    }

    private fun restoreImages(srcDir: File, destDir: File) {
        srcDir.listFiles()?.forEach { image ->
            image.copyTo(File(destDir, image.name), overwrite = true)
        }
    }
}