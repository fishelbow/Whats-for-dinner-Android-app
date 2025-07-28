package com.example.possiblythelastnewproject.backup

import android.content.Context
import android.net.Uri
import com.example.possiblythelastnewproject.core.data.AppDatabase
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

class ZipExporter @Inject constructor(
    private val context: Context,
    private val db: AppDatabase
) {
    private val json = Json { prettyPrint = true }

    suspend fun export(uri: Uri): String {
        val exportRoot = File(context.cacheDir, "exportTemp").apply { mkdirs() }
        val dataDir = File(exportRoot, "data").apply { mkdirs() }
        val mediaDir = File(exportRoot, "media").apply { mkdirs() }
        val zipFile = File(context.cacheDir, "backup.zip")

        return try {
            logPhase("📦 Starting export")

            dumpDatabase(dataDir)
            logPhase("📘 JSON dump complete")

            val copiedImages = collectImages(context.filesDir, mediaDir)
            logPhase("🖼 Media collected: $copiedImages files")

            writeMeta(exportRoot, copiedImages)
            logPhase("📝 Meta file written")

            zipDirectory(exportRoot, zipFile)
            logPhase("🗜 Files zipped")

            writeZipToDestination(uri, zipFile)
            logPhase("📤 Export written to URI")

            exportRoot.deleteRecursively()
            zipFile.delete()

            "✅ Exported successfully ($copiedImages images)"
        } catch (e: Exception) {
            "❌ Export failed: ${e.message}"
        }
    }

    private suspend fun dumpDatabase(target: File) {
        target.resolve("pantry_items.json").writeText(json.encodeToString(db.pantryItemDao().getAllOnce()))
        target.resolve("recipes.json").writeText(json.encodeToString(db.recipeDao().getAllOnce()))
        target.resolve("cross_refs.json").writeText(json.encodeToString(db.recipePantryItemDao().getAllOnce()))
        target.resolve("shopping_lists.json").writeText(json.encodeToString(db.shoppingListDao().getAllOnce()))
        target.resolve("shopping_list_items.json").writeText(json.encodeToString(db.shoppingListEntryDao().getAllOnce()))
        target.resolve("recipe_selections.json").writeText(json.encodeToString(db.recipeSelectionDao().getAllOnce()))
        target.resolve("undo_actions.json").writeText(json.encodeToString(db.undoDao().getAllOnce()))
        target.resolve("categories.json").writeText(json.encodeToString(db.categoryDao().getAllOnce()))
    }

    private fun collectImages(sourceDir: File, targetDir: File): Int {
        var count = 0
        sourceDir.walkTopDown().filter { it.isFile && it.extension.lowercase() == "jpg" }.forEach { file ->
            val relativePath = file.relativeTo(sourceDir).invariantSeparatorsPath
            val targetFile = File(targetDir, relativePath)
            targetFile.parentFile?.mkdirs()
            file.copyTo(targetFile, overwrite = true)
            count++
        }
        return count
    }

    @Serializable
    private data class BackupMeta(
        val timestamp: Long,
        val version: Int,
        val source: String,
        val mediaFileCount: Int
    )

    private fun writeMeta(root: File, mediaCount: Int) {
        val meta = BackupMeta(
            timestamp = System.currentTimeMillis(),
            version = 1,
            source = "PossiblyTheLastNewProject",
            mediaFileCount = mediaCount
        )
        root.resolve("meta.json").writeText(json.encodeToString(meta))
    }

    private fun zipDirectory(source: File, output: File) {
        ZipOutputStream(output.outputStream().buffered()).use { zip ->
            source.walkTopDown().filter { it.isFile }.forEach { file ->
                val entryName = file.relativeTo(source).invariantSeparatorsPath
                zip.putNextEntry(ZipEntry(entryName))
                file.inputStream().copyTo(zip)
                zip.closeEntry()
            }
        }
    }

    private fun writeZipToDestination(uri: Uri, zipFile: File) {
        context.contentResolver.openOutputStream(uri)?.use { out ->
            zipFile.inputStream().copyTo(out)
        } ?: throw IllegalStateException("Unable to open output stream for URI: $uri")
    }

    private fun logPhase(message: String) {
        println("🔹 $message")
    }
}