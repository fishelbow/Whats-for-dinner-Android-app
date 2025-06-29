package com.example.possiblythelastnewproject.core.data.sandbox

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File

object DbFileManager {

    private const val DB_NAME = "PossiblyTheLastNewProject_database"

    fun exportDatabase(context: Context, destinationUri: Uri): Boolean {
        return try {
            val dbFile = context.getDatabasePath(DB_NAME)
            Log.d("DbExport", "Exporting from: ${dbFile.absolutePath}")

            if (!dbFile.exists()) {
                Log.e("DbExport", "Database file does not exist!")
                return false
            }

            val before = dbFile.lastModified()
            Log.d("DbExport", "DB last modified: $before")

            context.contentResolver.openOutputStream(destinationUri)?.use { output ->
                dbFile.inputStream().copyTo(output)
            } ?: run {
                Log.e("DbExport", "Failed to open output stream for URI: $destinationUri")
                return false
            }

            Log.i("DbExport", "Export successful")
            true
        } catch (e: Exception) {
            Log.e("DbExport", "Export failed", e)
            false
        }
    }

    fun importDatabase(context: Context, sourceUri: Uri): Boolean {
        return try {
            val dbFile = context.getDatabasePath(DB_NAME)
            Log.d("DbImport", "Importing to: ${dbFile.absolutePath}")
            val before = dbFile.lastModified()
            Log.d("DbImport", "DB last modified before import: $before")

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                dbFile.outputStream().use { output -> input.copyTo(output) }
            } ?: run {
                Log.e("DbImport", "Failed to open input stream for URI: $sourceUri")
                return false
            }

            val after = dbFile.lastModified()
            Log.d("DbImport", "DB last modified after import: $after")

            if (after == before) {
                Log.w("DbImport", "Import may not have overwritten the file")
            }

            Log.i("DbImport", "Import successful")
            true
        } catch (e: Exception) {
            Log.e("DbImport", "Import failed", e)
            false
        }
    }
}