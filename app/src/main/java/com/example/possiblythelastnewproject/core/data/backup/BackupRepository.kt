package com.example.possiblythelastnewproject.core.data.backup

import android.app.Application
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject

class BackupRepository @Inject constructor(
    private val context: Application
) {

    fun readJsonFromUri(uri: Uri): Result<String> = runCatching {
        context.contentResolver.openInputStream(uri)
            ?.bufferedReader()
            ?.readText() ?: throw IOException("Unable to read file")
    }

    fun zipImages(files: List<File>, destinationZip: File): Result<File> = runCatching {
        val validFiles = files.filter { it.exists() }

        ZipOutputStream(FileOutputStream(destinationZip)).use { zipOut ->
            validFiles.forEach { file ->
                zipOut.putNextEntry(ZipEntry(file.name))
                file.inputStream().use { it.copyTo(zipOut) }
                zipOut.closeEntry()
            }
        }

        destinationZip
    }

    fun writeJsonToDownloads(json: String, fileName: String = "pantry_backup.json"): Result<File> = runCatching {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)
        FileOutputStream(file, false).use { it.write(json.toByteArray()) }

        scanFile(file, mimeType = "application/json")
        file
    }

    fun scanFile(file: File, mimeType: String) {
        MediaScannerConnection.scanFile(
            context,
            arrayOf(file.absolutePath),
            arrayOf(mimeType),
            null
        )
    }

    fun getImageFiles(): List<File> {
        val imageDir = File(context.filesDir, "images")
        return imageDir.listFiles()?.filter { it.exists() } ?: emptyList()
    }

    fun extractZipToTemp(uri: Uri, tempDir: File): Result<Unit> = runCatching {
        context.contentResolver.openInputStream(uri)?.use { input ->
            ZipInputStream(input).use { zipIn ->
                var entry = zipIn.nextEntry
                while (entry != null) {
                    val outFile = File(tempDir, entry.name).apply {
                        parentFile?.mkdirs()
                    }
                    if (!entry.isDirectory) {
                        outFile.outputStream().use { zipIn.copyTo(it) }
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }
        } ?: throw IOException("Could not open zip input stream")
    }

    fun restoreExtractedImages(srcDir: File, destDir: File): Int {
        if (!srcDir.exists()) return 0
        destDir.mkdirs()
        val images = srcDir.listFiles()?.filter { it.isFile } ?: emptyList()
        images.forEach { image ->
            image.copyTo(File(destDir, image.name), overwrite = true)
        }
        return images.size
    }
}