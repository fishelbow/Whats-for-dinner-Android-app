package com.example.possiblythelastnewproject.backup

import android.content.Context
import android.net.Uri
import com.example.possiblythelastnewproject.core.data.AppDatabase
import com.example.possiblythelastnewproject.features.pantry.data.entities.*
import com.example.possiblythelastnewproject.features.recipe.data.entities.*
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.*
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.util.zip.ZipInputStream
import javax.inject.Inject


data class ProgressSpan(val start: Float, val end: Float) {
    fun scale(pct: Float): Float = (start + pct * (end - start)).coerceIn(start, end)
}

class ZipImporter @Inject constructor(
    private val context: Context,
    private val db: AppDatabase
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun import(uri: Uri, onProgress: (Float, String) -> Unit = { _, _ -> }): String {
        val importRoot = File(context.cacheDir, "importTemp").apply { mkdirs() }
        val dataDir = File(importRoot, "data")
        val mediaDir = File(importRoot, "media")

        val spanSetup = ProgressSpan(0f, 0.03f)
        val spanZip = ProgressSpan(0.03f, 0.13f)
        val spanDB = ProgressSpan(0.13f, 0.73f)
        val spanMedia = ProgressSpan(0.73f, 0.93f)
        val spanCleanup = ProgressSpan(0.93f, 1.0f)

        fun step(span: ProgressSpan, pct: Float, msg: String) {
            val scaled = span.scale(pct)
            onProgress(scaled, msg)
            logPhase(msg)
        }

        return try {
            step(spanSetup, 0.005f, "📦 Starting import")
            dataDir.mkdirs(); step(spanSetup, 0.015f, "📘 Data folder ready")
            mediaDir.mkdirs(); step(spanSetup, 0.027f, "🖼 Media folder ready")
            step(spanSetup, 0.03f, "🧱 Starting Zip Extraction please wait")

            unzipToDirectory(uri, importRoot, spanZip, onProgress)
            step(spanZip, 1.0f, "🗃 Zip extraction complete")

            restoreDatabase(dataDir) { pct, msg -> onProgress(spanDB.scale(pct), msg) }
            step(spanDB, 1.0f, "📘 Database restored")

            restoreMedia(mediaDir, spanMedia) { pct, msg -> onProgress(spanMedia.scale(pct), msg) }
            step(spanMedia, 1.0f, "🖼 Media restored")

            importRoot.deleteRecursively()
            step(spanCleanup, 1.0f, "🧼 Cleanup complete")
            "✅ Imported successfully"
        } catch (e: Exception) {
            onProgress(1f, "❌ Import failed")
            "❌ Import failed: ${e.message}"
        }
    }

    private fun unzipToDirectory(uri: Uri, targetDir: File, span: ProgressSpan, onProgress: (Float, String) -> Unit) {
        val rawStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Couldn't open input stream for URI")

        // Step 1: Count total entries
        val totalEntries = ZipInputStream(rawStream).use { zip ->
            var count = 0
            while (zip.nextEntry != null) {
                count++
                zip.closeEntry()
            }
            count
        }.coerceAtLeast(1) // avoid divide-by-zero

        // Step 2: Actual extraction pass
        context.contentResolver.openInputStream(uri)?.use { input ->
            ZipInputStream(input).use { zip ->
                var index = 0
                var entry = zip.nextEntry

                while (entry != null) {
                    val outFile = File(targetDir, entry.name)
                    outFile.parentFile?.mkdirs()
                    outFile.outputStream().use { zip.copyTo(it) }

                    val ext = File(entry.name).extension.lowercase()
                    val emoji = when (ext) {
                        "json", "ndjson" -> "📘"
                        "jpg", "jpeg", "png" -> "🖼"
                        else -> "📦"
                    }
                    val msg = "$emoji Extracted ${entry.name} (${index + 1}/$totalEntries)"
                    onProgress(span.scale(index.toFloat() / totalEntries), msg)

                    if (index == 0) onProgress(span.scale(0.07f), "🔧First entry written")
                    if (index == 1) onProgress(span.scale(0.085f), "⏳Continuing extraction…")

                    zip.closeEntry()
                    entry = zip.nextEntry
                    index++
                }
            }
        }
    }

    private suspend fun restoreDatabase(dataDir: File, onProgress: (Float, String) -> Unit) {
        val sections = listOf(
            ::restoreCategories, ::restorePantryItems, ::restoreRecipes,
            ::restoreCrossRefs, ::restoreShoppingLists, ::restoreShoppingListItems,
            ::restoreSelections, ::restoreUndoActions
        )
        val count = sections.size
        sections.forEachIndexed { index, restoreFn ->
            val base = index / count.toFloat()
            val label = restoreFn.name.removePrefix("restore").replace(Regex("([a-z])([A-Z])"), "$1 $2")
            onProgress(base, "📘 Starting $label…")
            restoreFn(dataDir, base, count) { pct, msg ->
                val scaled = base + (pct / count.toFloat())
                onProgress(scaled.coerceIn(0f, 1f), msg)
            }
        }
    }

    private suspend fun restoreCategories(dataDir: File, base: Float, count: Int, onProgress: (Float, String) -> Unit) {
        val list = decodeStream<Category>(File(dataDir, "categories.ndjson"), onProgress)
        list.forEachIndexedProgress("📘 Inserting category", base, count, onProgress) {
            db.categoryDao().insertCategory(it)
        }
    }

    private suspend fun restorePantryItems(dataDir: File, base: Float, count: Int, onProgress: (Float, String) -> Unit) {
        val list = decodeStream<PantryItem>(File(dataDir, "pantry_items.ndjson"), onProgress)
        list.forEachIndexedProgress("📘 Inserting pantry item", base, count, onProgress) {
            db.pantryItemDao().insertPantryItem(it)
        }
    }

    private suspend fun restoreRecipes(dataDir: File, base: Float, count: Int, onProgress: (Float, String) -> Unit) {
        val list = decodeStream<Recipe>(File(dataDir, "recipes.ndjson"), onProgress)
        list.forEachIndexedProgress("📘 Inserting recipe", base, count, onProgress) {
            db.recipeDao().insertRecipe(it)
        }
    }

    private suspend fun restoreCrossRefs(dataDir: File, base: Float, count: Int, onProgress: (Float, String) -> Unit) {
        val list = decodeStream<RecipePantryItemCrossRef>(File(dataDir, "cross_refs.ndjson"), onProgress)
        simulateProgressTracking(list.size, "📘 Inserting cross refs", base, count, onProgress)
        db.recipePantryItemDao().insertAll(list)
    }

    private suspend fun restoreShoppingLists(dataDir: File, base: Float, count: Int, onProgress: (Float, String) -> Unit) {
        val list = decodeStream<ShoppingList>(File(dataDir, "shopping_lists.ndjson"), onProgress)
        simulateProgressTracking(list.size, "🛒 Inserting shopping lists", base, count, onProgress)
        db.shoppingListDao().insertAll(list)
    }

    private suspend fun restoreShoppingListItems(dataDir: File, base: Float, count: Int, onProgress: (Float, String) -> Unit) {
        val list = decodeStream<ShoppingListItem>(File(dataDir, "shopping_list_items.ndjson"), onProgress)
        simulateProgressTracking(list.size, "🧾 Inserting shopping list items", base, count, onProgress)
        db.shoppingListEntryDao().insertAll(list)
    }

    private suspend fun restoreSelections(dataDir: File, base: Float, count: Int, onProgress: (Float, String) -> Unit) {
        val list = decodeStream<RecipeSelection>(File(dataDir, "recipe_selections.ndjson"), onProgress)
        simulateProgressTracking(list.size, "📘 Inserting recipe selections", base, count, onProgress)
        db.recipeSelectionDao().insertAll(list)
    }

    private suspend fun restoreUndoActions(dataDir: File, base: Float, count: Int, onProgress: (Float, String) -> Unit) {
        val list = decodeStream<UndoAction>(File(dataDir, "undo_actions.ndjson"), onProgress)
        simulateProgressTracking(list.size, "↩️ Inserting undo actions", base, count, onProgress)
        db.undoDao().insertAll(list)
    }

    private fun restoreMedia(sourceDir: File, span: ProgressSpan, onProgress: (Float, String) -> Unit) {
        val bufferSize = 64 * 1024
        val flushInterval = 500
        val startTime = System.currentTimeMillis()
        var index = 0
        val images = sourceDir.walkTopDown().filter { it.isFile && it.extension.lowercase() == "jpg" }
        val total = images.count().coerceAtLeast(1)

        images.forEach { file ->
            try {
                val target = File(context.filesDir, file.name)
                file.inputStream().buffered(bufferSize).use { input ->
                    target.outputStream().buffered(bufferSize).use { output ->
                        input.copyTo(output)
                    }
                }
                val progressPct = index.toFloat() / total
                val scaled = span.scale(progressPct)

                if (index % flushInterval == 0 || index < 3) {
                    val elapsedMs = System.currentTimeMillis() - startTime
                    val etaMs = (elapsedMs.toFloat() / (index + 1)) * total - elapsedMs
                    val etaMin = (etaMs / 1000 / 60).toInt()
                    onProgress(scaled, "🖼Copied ${file.name} (${index + 1}/$total)\n⏳ETA ~${etaMin}min")
                }

                if (index < 3) {
                    val sizeKb = file.length() / 1024
                    onProgress(span.scale(progressPct + 0.001f), "📥Copied ${sizeKb}KB")
                }

                index++
            } catch (e: Exception) {
                logPhase("⚠️Failed ${file.name}: ${e.message}")
                e.printStackTrace()
                onProgress(span.scale(1.0f), "⚠️Failed ${file.name}: ${e.message}")
            }
        }
    }

    private suspend fun <T> List<T>.forEachIndexedProgress(
        labelPrefix: String,
        base: Float,
        count: Int,
        onProgress: (Float, String) -> Unit,
        handler: suspend (T) -> Unit
    ) {
        val total = size.coerceAtLeast(1)
        forEachIndexed { i, item ->
            val pct = i / total.toFloat()
            val scaled = base + (pct / count.toFloat())
            val msg = "$labelPrefix (${i + 1}/$total)"
            onProgress(scaled.coerceIn(0f, 1f), msg)
            handler(item)
        }
    }

    private fun simulateProgressTracking(
        itemCount: Int,
        labelPrefix: String,
        base: Float,
        count: Int,
        onProgress: (Float, String) -> Unit
    ) {
        val safeTotal = itemCount.coerceAtLeast(1)
        repeat(itemCount) { i ->
            val pct = i / safeTotal.toFloat()
            val scaled = base + (pct / count.toFloat())
            val msg = "$labelPrefix (${i + 1}/$itemCount)"
            onProgress(scaled.coerceIn(0f, 1f), msg)
        }
    }

    private fun logPhase(message: String) {
        println("🔹 $message")
    }

    @Serializable
    data class BackupMeta(val timestamp: Long, val version: Int, val source: String)

    @OptIn(ExperimentalSerializationApi::class)
    private inline fun <reified T> decodeStream(file: File, crossinline onProgress: (Float, String) -> Unit): List<T> {
        val ext = file.extension.lowercase()
        return if (ext == "ndjson") {
            val lineSeq = file.bufferedReader().lineSequence()
            val result = mutableListOf<T>()
            var index = 0
            var totalEst = 0

            lineSeq.forEach {
                totalEst++ // count as we go, just for x/x
            }

            file.bufferedReader().lineSequence().forEachIndexed { idx, line ->
                val pct = idx.toFloat() / totalEst.coerceAtLeast(1)
                val msg = "📘Parsing ${file.name} (${idx + 1}/$totalEst)"
                onProgress(pct, msg)
                try {
                    result += json.decodeFromString(serializer<T>(), line)
                } catch (e: Exception) {
                    logPhase("⚠️Malformed NDJSON line $idx: ${e.message}")
                }
            }
            result
        } else {
            onProgress(0f, "📘Parsing ${file.name} as JSON array (${file.length()} bytes)")
            file.inputStream().use {
                json.decodeFromStream(ListSerializer(serializer<T>()), it)
            }
        }
    }
}