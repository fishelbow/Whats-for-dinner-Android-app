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
            step(spanSetup, 0.008f, "🧭 Locating cache directory…")

            val importRoot = File(context.cacheDir, "importTemp").apply {
                step(spanSetup, 0.012f, "📁 Creating importTemp folder…")
                mkdirs()
                step(spanSetup, 0.015f, "📁 importTemp ready")
            }

            val dataDir = File(importRoot, "data").apply {
                step(spanSetup, 0.018f, "📘 Creating data folder…")
                mkdirs()
                step(spanSetup, 0.021f, "📘 Data folder ready")
            }

            val mediaDir = File(importRoot, "media").apply {
                step(spanSetup, 0.024f, "🖼 Creating media folder…")
                mkdirs()
                step(spanSetup, 0.027f, "🖼 Media folder ready")
            }

            step(spanSetup, 0.03f, "🧱 Folder prep complete")

            unzipToDirectory(uri, importRoot, spanZip, onProgress)
            step(spanZip, 1.0f, "🗃 Zip extraction complete")

            restoreDatabase(dataDir) { pct, msg -> onProgress(spanDB.scale(pct), msg) }
            step(spanDB, 1.0f, "📘 Database restored")

            restoreMedia(mediaDir, spanMedia) { pct, msg ->
                onProgress(spanMedia.scale(pct), msg)
            }
            step(spanMedia, 1.0f, "🖼 Media restored")

            importRoot.deleteRecursively()
            step(spanCleanup, 1.0f, "🧼 Cleanup complete")

            "✅ Imported successfully"
        } catch (e: Exception) {
            onProgress(1f, "❌ Import failed")
            "❌ Import failed: ${e.message}"
        }
    }

    private fun unzipToDirectory(
        uri: Uri,
        targetDir: File,
        span: ProgressSpan,
        onProgress: (Float, String) -> Unit
    ) {
        val minBoot = span.scale(0.00f)
        val midBoot = span.scale(0.02f)
        val preScan = span.scale(0.04f)
        val postScan = span.scale(0.06f)

        // 🟡 Stream Initialization Phase
        onProgress(minBoot, "🔌 Requesting archive stream…")

        val rawInputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Couldn't open input stream for URI")
        onProgress(midBoot, "📂 Archive stream opened")

        val zipStream = ZipInputStream(rawInputStream)
        onProgress(preScan, "🔍 Zip reader initialized")

        // 📊 Optional Size Report (if accessible)
        val readableSizeMB = try {
            val size = context.contentResolver.openFileDescriptor(uri, "r")?.statSize ?: -1
            (size / 1024 / 1024).coerceAtLeast(1)
        } catch (e: Exception) { -1 }
        if (readableSizeMB > 0) {
            onProgress(span.scale(0.05f), "📊 Archive size: ${readableSizeMB}MB")
        }

        // 📦 Scan Entry Names
        val entryNames = mutableListOf<String>()
        var scanEntry = zipStream.nextEntry
        var scanned = 0
        val scanBumps = 20  // emit ~20 scan messages max
        while (scanEntry != null) {
            entryNames.add(scanEntry.name)
            scanned++

            // ✅ Create local variable
            val total = scanned.coerceAtLeast(1)
            onProgress(postScan, "📦 Found $total entries in archive")


            // Emit status every N entries
            if (scanned % (total / scanBumps).coerceAtLeast(1) == 0) {
                val pct = scanned / (scanBumps * 1f)
                val progress = span.scale(0.03f + pct * 0.03f) // fills 3.0–6.0%
                val hint = if (scanEntry.name.endsWith(".json")) "📘" else "📁"
                val label = "$hint Scanned ${scanEntry.name} (${scanned} entries)"
                onProgress(progress, label)
            }

            zipStream.closeEntry()
            scanEntry = zipStream.nextEntry
        }

        val total = entryNames.size.coerceAtLeast(1)
        onProgress(postScan, "📦 Found $total entries in archive")

        // 🔁 Reopen stream for extraction
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
                        "json" -> "📘"
                        "jpg", "jpeg", "png" -> "🖼"
                        else -> "📦"
                    }

                    val pct = index / total.toFloat()
                    val msg = "$emoji Extracted ${entry.name} (${index + 1}/$total)"
                    onProgress(span.scale(pct), msg)

                    if (index == 0) onProgress(span.scale(0.07f), "🔧 First entry written")
                    if (index == 1) onProgress(span.scale(0.085f), "⏳ Continuing extraction…")

                    index++
                    zip.closeEntry()
                    entry = zip.nextEntry
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
            val label = restoreFn.name.removePrefix("restore")
                .replace(Regex("([a-z])([A-Z])"), "$1 $2")
            onProgress(base, "📘 Starting $label…")
            restoreFn(dataDir, base, count) { pct, msg ->
                val scaled = base + (pct / count.toFloat())
                onProgress(scaled.coerceIn(0f, 1f), msg)
            }
        }
    }

    private suspend fun restoreCategories(dataDir: File, base: Float, count: Int, onProgress: (Float, String) -> Unit) {
        val list = decodeStream<Category>(File(dataDir, "categories.json"), onProgress)
        list.forEachIndexedProgress("📘 Inserting category", base, count, onProgress) {
            db.categoryDao().insertCategory(it)
        }
    }

    private suspend fun restorePantryItems(dataDir: File, base: Float, count: Int, onProgress: (Float, String) -> Unit) {
        val list = decodeStream<PantryItem>(File(dataDir, "pantry_items.json"), onProgress)
        list.forEachIndexedProgress("📘 Inserting pantry item", base, count, onProgress) {
            db.pantryItemDao().insertPantryItem(it)
        }
    }

    private suspend fun restoreRecipes(dataDir: File, base: Float, count: Int, onProgress: (Float, String) -> Unit) {
        val file = File(dataDir, "recipes.json")
        onProgress(0f, "📘 Parsing recipes.json (${file.length()} bytes)")
        val list = decodeStream<Recipe>(file)
        val total = list.size.coerceAtLeast(1)
        list.forEachIndexed { i, item ->
            val pct = i / total.toFloat()
            val scaled = base + (pct / count.toFloat())
            onProgress(scaled.coerceIn(0f, 1f), "📘 Inserting recipe ${item.name} (${i + 1}/$total)")
            db.recipeDao().insertRecipe(item)
        }
    }

    private suspend fun restoreCrossRefs(dataDir: File, base: Float, count: Int, onProgress: (Float, String) -> Unit) {
        val list = decodeStream<RecipePantryItemCrossRef>(File(dataDir, "cross_refs.json"), onProgress)
        simulateProgressTracking(list.size, "📘 Inserting cross refs", base, count, onProgress)
        db.recipePantryItemDao().insertAll(list)
    }

    private suspend fun restoreShoppingLists(dataDir: File, base: Float, count: Int, onProgress: (Float, String) -> Unit) {
        val list = decodeStream<ShoppingList>(File(dataDir, "shopping_lists.json"), onProgress)
        simulateProgressTracking(list.size, "🛒 Inserting shopping lists", base, count, onProgress)
        db.shoppingListDao().insertAll(list)
    }

    private suspend fun restoreShoppingListItems(dataDir: File, base: Float, count: Int, onProgress: (Float, String) -> Unit) {
        val list = decodeStream<ShoppingListItem>(File(dataDir, "shopping_list_items.json"), onProgress)
        simulateProgressTracking(list.size, "🧾 Inserting shopping list items", base, count, onProgress)
        db.shoppingListEntryDao().insertAll(list)
    }

    private suspend fun restoreSelections(dataDir: File, base: Float, count: Int, onProgress: (Float, String) -> Unit) {
        val list = decodeStream<RecipeSelection>(File(dataDir, "recipe_selections.json"), onProgress)
        simulateProgressTracking(list.size, "📘 Inserting recipe selections", base, count, onProgress)
        db.recipeSelectionDao().insertAll(list)
    }

    private suspend fun restoreUndoActions(dataDir: File, base: Float, count: Int, onProgress: (Float, String) -> Unit) {
        val list = decodeStream<UndoAction>(File(dataDir, "undo_actions.json"), onProgress)
        simulateProgressTracking(list.size, "↩️ Inserting undo actions", base, count, onProgress)
        db.undoDao().insertAll(list)
    }

    private fun restoreMedia(sourceDir: File, span: ProgressSpan, onProgress: (Float, String) -> Unit) {
        val images = sourceDir.listFiles()?.filter { it.extension == "jpg" } ?: return
        val total = images.size.coerceAtLeast(1)

        images.forEachIndexed { i, file ->
            val pct = i / total.toFloat()
            val progress = span.scale(pct)
            val msg = "🖼 Importing ${file.name} (${i + 1}/$total)"
            onProgress(progress, msg)

            file.copyTo(File(context.filesDir, file.name), overwrite = true)

            if (i < 3) {
                onProgress(span.scale(pct + 0.001f), "📥 Copied ${file.length()} bytes")
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private inline fun <reified T> decodeStream(file: File): List<T> =
        if (file.exists()) file.inputStream().use {
            json.decodeFromStream(ListSerializer(serializer<T>()), it)
        } else emptyList()

    private inline fun <reified T> decodeStream(file: File, onProgress: (Float, String) -> Unit): List<T> {
        onProgress(0f, "📘 Parsing ${file.name} (${file.length()} bytes)")
        return decodeStream(file)
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

    data class ProgressSpan(val start: Float, val end: Float) {
        fun scale(pct: Float): Float = (start + pct * (end - start)).coerceIn(start, end)
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
    data class BackupMeta(
        val timestamp: Long,
        val version: Int,
        val source: String
    )
}