package com.example.possiblythelastnewproject.core.data.backup

import android.app.Application
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

class BackupRepository @Inject constructor(
    private val context: Application
) {
    fun readJsonFromUri(uri: Uri): Result<String> = runCatching {
        context.contentResolver.openInputStream(uri)
            ?.bufferedReader()
            ?.readText() ?: throw IOException("Unable to read file")
    }

    fun writeJsonToDownloads(json: String, fileName: String = "pantry_backup.json"): Result<File> = runCatching {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)
        FileOutputStream(file, false).use { it.write(json.toByteArray()) }
        MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("application/json"), null)
        file
    }
}