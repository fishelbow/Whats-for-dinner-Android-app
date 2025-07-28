package com.example.possiblythelastnewproject.backup

import android.content.Context
import android.net.Uri
import com.example.possiblythelastnewproject.core.data.AppDatabase
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.util.zip.ZipInputStream
import javax.inject.Inject

class ZipImporter @Inject constructor(
    private val context: Context,
    private val db: AppDatabase
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun import(uri: Uri): String {
        val importRoot = File(context.cacheDir, "importTemp").apply { mkdirs() }
        val dataDir = File(importRoot, "data")
        val mediaDir = File(importRoot, "media")

        return try {
            logPhase("📦 Starting import")

            unzipToDirectory(uri, importRoot)
            logPhase("🗃 Unzipped archive")

            restoreDatabase(dataDir)
            logPhase("📘 Database restored")

            restoreMedia(mediaDir)
            logPhase("🖼 Media restored")

            importRoot.deleteRecursively()
            logPhase("🧼 Cleanup complete")

            "✅ Imported successfully"
        } catch (e: Exception) {
            "❌ Import failed: ${e.message}"
        }
    }

    private suspend fun restoreDatabase(dataDir: File) {
        db.categoryDao().insertAll(readJson(dataDir, "categories.json"))
        db.pantryItemDao().insertAll(readJson(dataDir, "pantry_items.json"))
        db.recipeDao().insertAll(readJson(dataDir, "recipes.json"))
        db.recipePantryItemDao().insertAll(readJson(dataDir, "cross_refs.json"))
        db.shoppingListDao().insertAll(readJson(dataDir, "shopping_lists.json"))
        db.shoppingListEntryDao().insertAll(readJson(dataDir, "shopping_list_items.json"))
        db.recipeSelectionDao().insertAll(readJson(dataDir, "recipe_selections.json"))
        db.undoDao().insertAll(readJson(dataDir, "undo_actions.json"))
    }

    private fun restoreMedia(sourceDir: File) {
        sourceDir.listFiles()?.forEach { file ->
            if (file.extension == "jpg") {
                file.copyTo(File(context.filesDir, file.name), overwrite = true)
            }
        }
    }

    private inline fun <reified T> readJson(sourceDir: File, filename: String): List<T> {
        val file = File(sourceDir, filename)
        return if (file.exists()) {
            json.decodeFromString(file.readText())
        } else emptyList()
    }

    private fun unzipToDirectory(uri: Uri, targetDir: File) {
        context.contentResolver.openInputStream(uri)?.use { input ->
            ZipInputStream(input).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val outFile = File(targetDir, entry.name)
                    outFile.parentFile?.mkdirs()
                    outFile.outputStream().use { zip.copyTo(it) }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        } ?: throw IllegalStateException("Could not open input stream for $uri")
    }

    private fun logPhase(message: String) {
        println("🔹 $message") // Optionally redirect to UI or overlay callback
    }

    @Serializable
    data class BackupMeta(
        val timestamp: Long,
        val version: Int,
        val source: String
    )
}