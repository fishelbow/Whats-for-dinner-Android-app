package com.example.possiblythelastnewproject.core.data.backup

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.possiblythelastnewproject.core.data.AppDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

@HiltViewModel
class DbBackupViewModel @Inject constructor(
    private val backupRepo: BackupRepository,
    private val serializer: BackupSerializer,
    private val importer: BackupImporter,
    private val db: AppDatabase,
    private val context: Application
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var result: String? by mutableStateOf(null)
        private set

    fun clearResult() {
        result = null
    }

    fun exportBackup(uri: Uri) {
        // alias to unified export
        exportUnifiedBackup(uri)
    }

    fun importBackup(uri: Uri) {
        // alias to unified import
        importUnifiedBackup(uri)
    }

    private fun exportUnifiedBackup(uri: Uri) {
        launchWithLoading {
            val backup = buildBackup()
            val json = serializer.serialize(backup)
            val imageFiles = backupRepo.getImageFiles()

            val resultFile = runCatching {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    ZipOutputStream(output).use { zipOut ->
                        zipOut.putNextEntry(ZipEntry("backup.json"))
                        zipOut.write(json.toByteArray())
                        zipOut.closeEntry()

                        imageFiles.forEach { image ->
                            zipOut.putNextEntry(ZipEntry("images/${image.name}"))
                            image.inputStream().use { it.copyTo(zipOut) }
                            zipOut.closeEntry()
                        }
                    }
                } ?: throw IOException("Could not open zip output stream")
            }

            result = resultFile.fold(
                onSuccess = { "✅ Unified backup saved to: ${uri.lastPathSegment} (${imageFiles.size} image(s) + DB state)" },
                onFailure = { "❌ Export failed: ${it.localizedMessage}" }
            )
        }
    }

    private fun importUnifiedBackup(uri: Uri) {
        launchWithLoading {
            val tempDir = File(context.cacheDir, "restore_${System.currentTimeMillis()}").apply { mkdirs() }
            val zipResult = backupRepo.extractZipToTemp(uri, tempDir)

            if (zipResult.isFailure) {
                result = "❌ Failed to unpack archive: ${zipResult.exceptionOrNull()?.localizedMessage}"
                return@launchWithLoading
            }

            val jsonFile = File(tempDir, "backup.json")
            if (!jsonFile.exists()) {
                result = "❌ Missing backup.json in archive"
                return@launchWithLoading
            }

            val backupResult = serializer.deserialize(jsonFile.readText())
            if (backupResult.isFailure) {
                result = "❌ Failed to parse backup: ${backupResult.exceptionOrNull()?.localizedMessage}"
                return@launchWithLoading
            }

            val importResult = importer.import(backupResult.getOrThrow())
            val imageRestoreCount = backupRepo.restoreExtractedImages(
                tempDir.resolve("images"),
                File(context.filesDir, "images")
            )

            result = buildString {
                appendLine("✅ Import complete:")
                appendLine("• Pantry items added: ${importResult.pantryItems}")
                appendLine("• Recipes added: ${importResult.recipes}")
                appendLine("• Categories added: ${importResult.categories}")
                appendLine("• Recipe refs added: ${importResult.refs}")
                appendLine("• Shopping lists added: ${importResult.lists}")
                appendLine("• Shopping entries added: ${importResult.entries}")
                appendLine("• Selections added: ${importResult.selections}")
                appendLine("• Undo actions added: ${importResult.undoActions}")
                appendLine("• Images restored: $imageRestoreCount")
            }
        }
    }

    private suspend fun buildBackup(): FullDatabaseBackup = withContext(Dispatchers.IO) {
        FullDatabaseBackup(
            version = 1,
            pantryItems = db.pantryItemDao().getAllOnce(),
            recipes = db.recipeDao().getAllOnce(),
            recipePantryRefs = db.recipePantryItemDao().getAllOnce(),
            shoppingLists = db.shoppingListDao().getAllOnce(),
            shoppingListItems = db.shoppingListEntryDao().getAllOnce(),
            categories = db.categoryDao().getAllOnce(),
            recipeSelections = db.recipeSelectionDao().getAllOnce(),
            undoActions = db.undoDao().getAllOnce()
        )
    }

    private fun launchWithLoading(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            try {
                block()
            } catch (e: Exception) {
                result = "❌ Error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun exportJson(uri: Uri) {
        exportUnifiedBackup(uri) // Now includes JSON + image bundle
    }

    fun importJson(uri: Uri) {
        importUnifiedBackup(uri) // Restores both database state and image assets
    }
}