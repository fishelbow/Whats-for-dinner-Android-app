package com.example.possiblythelastnewproject.backup

import android.content.Context
import android.net.Uri
import com.example.possiblythelastnewproject.core.data.AppDatabase
import com.example.possiblythelastnewproject.features.pantry.data.entities.Category
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.recipe.data.entities.Recipe
import com.example.possiblythelastnewproject.features.recipe.data.entities.RecipePantryItemCrossRef
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.RecipeSelection
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.ShoppingList
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.ShoppingListItem
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.UndoAction
import kotlinx.serialization.KSerializer
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

    suspend fun export(
        uri: Uri,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): String = try {
        onProgress(0f, "📦 Starting export")
        val exportRoot = prepareExportFolders()

        exportDatabaseChunked(exportRoot.resolve("data")) { msg, pct ->
            onProgress(pct * 0.6f, msg) // 0–60%
        }

        onProgress(0.65f, "🖼 Copying images...")
        val imageCount = collectImagesChunked(
            sourceDir = context.filesDir,
            targetDir = exportRoot.resolve("media"),
            chunkSize = 100
        ) { copied, total ->
            val pct = 0.6f + (copied.toFloat() / total.toFloat() * 0.25f)
            onProgress(pct, "🖼 Copied $copied / $total images")
        }

        onProgress(0.9f, "📝 Writing metadata")
        writeMeta(exportRoot, imageCount)

        zipDirectoryToDestination(exportRoot, uri) { entryName ->
            onProgress(0.95f, "📁 Zipped: $entryName")
        }

        onProgress(1f, "✅ Export complete")
        exportRoot.deleteRecursively()
        "✅ Exported successfully ($imageCount images)"
    } catch (e: Exception) {
        e.printStackTrace()
        onProgress(1f, "❌ Export failed: ${e.message}")
        "❌ Export failed: ${e.message}"
    }

    private fun prepareExportFolders(): File {
        val root = File(context.cacheDir, "exportTemp").apply { mkdirs() }
        File(root, "data").mkdirs()
        File(root, "media").mkdirs()
        return root
    }

    private suspend fun <T> dumpPagedData(
        file: File,
        limit: Int,
        label: String,
        serializer: KSerializer<T>,
        fetchPaged: suspend (limit: Int, offset: Int) -> List<T>,
        onStatus: (String) -> Unit = {}
    ) {
        file.bufferedWriter().use { writer ->
            writer.write("[")
            var offset = 0
            var first = true
            var total = 0
            while (true) {
                val chunk = fetchPaged(limit, offset)
                if (chunk.isEmpty()) break
                for (item in chunk) {
                    if (!first) writer.write(",") else first = false
                    writer.write(json.encodeToString(serializer, item))
                    total++
                }
                onStatus("📘 [$label] Exported $total items so far")
                offset += limit
            }
            writer.write("]")
            onStatus("✅ [$label] Finished $total total")
        }
    }

    private suspend fun exportDatabaseChunked(
        dataDir: File,
        onStatus: (msg: String, pct: Float) -> Unit = { _, _ -> }
    ) {
        val daoCount = 8
        var currentDao = 0

        suspend fun <T> nextProgress(
            label: String,
            fileName: String,
            serializer: KSerializer<T>,
            fetch: suspend (Int, Int) -> List<T>
        ) {
            dumpPagedData(dataDir.resolve(fileName), 5000, label, serializer, fetch) { msg ->
                val pct = currentDao.toFloat() / daoCount
                onStatus(msg, pct)
            }
            currentDao++
        }

        nextProgress("Pantry Items", "pantry_items.json", PantryItem.serializer()) { l, o -> db.pantryItemDao().getPaged(l, o) }
        nextProgress("Recipes", "recipes.json", Recipe.serializer()) { l, o -> db.recipeDao().getPaged(l, o) }
        nextProgress("Recipe↔Pantry Links", "cross_refs.json", RecipePantryItemCrossRef.serializer()) { l, o -> db.recipePantryItemDao().getPaged(l, o) }
        nextProgress("Shopping Lists", "shopping_lists.json", ShoppingList.serializer()) { l, o -> db.shoppingListDao().getPaged(l, o) }
        nextProgress("Shopping Items", "shopping_list_items.json", ShoppingListItem.serializer()) { l, o -> db.shoppingListEntryDao().getPaged(l, o) }
        nextProgress("Recipe Selections", "recipe_selections.json", RecipeSelection.serializer()) { l, o -> db.recipeSelectionDao().getPaged(l, o) }
        nextProgress("Undo Actions", "undo_actions.json", UndoAction.serializer()) { l, o -> db.undoDao().getPaged(l, o) }
        nextProgress("Categories", "categories.json", Category.serializer()) { l, o -> db.categoryDao().getPaged(l, o) }
    }

    private fun collectImagesChunked(
        sourceDir: File,
        targetDir: File,
        chunkSize: Int,
        onProgress: (copied: Int, total: Int) -> Unit
    ): Int {
        val jpgFiles = sourceDir.walkTopDown()
            .filter { it.isFile && it.extension.lowercase() == "jpg" }
            .toList()

        val total = jpgFiles.size
        var copied = 0

        jpgFiles.chunked(chunkSize).forEach { batch ->
            batch.forEach { file ->
                val relative = file.relativeTo(sourceDir).invariantSeparatorsPath
                val targetFile = File(targetDir, relative)
                targetFile.parentFile?.mkdirs()
                file.copyTo(targetFile, overwrite = true)
                copied++
            }
            onProgress(copied, total)
        }
        return copied
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

    private fun zipDirectoryToDestination(
        source: File,
        destinationUri: Uri,
        onEachFile: (String) -> Unit = {}
    ) {
        val outStream = context.contentResolver.openOutputStream(destinationUri)
            ?: throw IllegalStateException("Unable to open URI stream")

        ZipOutputStream(outStream.buffered()).use { zip ->
            source.walkTopDown().filter { it.isFile }.forEach { file ->
                val entryName = file.relativeTo(source).invariantSeparatorsPath
                zip.putNextEntry(ZipEntry(entryName))
                file.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
                onEachFile(entryName)
            }
        }
    }
}