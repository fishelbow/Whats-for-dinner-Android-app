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
import java.io.IOException
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

    fun exportJson(uri: Uri) {
        launchWithLoading {
            val backup = buildBackup()
            val json = serializer.serialize(backup)

            val resultFile = runCatching {
                context.contentResolver.openOutputStream(uri)?.use {
                    it.write(json.toByteArray())
                } ?: throw IOException("Could not open output stream")
            }

            result = resultFile.fold(
                onSuccess = { "✅ Exported to: ${uri.lastPathSegment}" },
                onFailure = { "❌ Failed to export: ${it.localizedMessage}" }
            )
        }
    }

    fun importJson(uri: Uri) {
        launchWithLoading {
            val jsonResult = backupRepo.readJsonFromUri(uri)
            if (jsonResult.isFailure) {
                result = "❌ Failed to read file: ${jsonResult.exceptionOrNull()?.localizedMessage}"
                return@launchWithLoading
            }

            val backupResult = serializer.deserialize(jsonResult.getOrThrow())
            if (backupResult.isFailure) {
                result = "❌ Failed to parse backup: ${backupResult.exceptionOrNull()?.localizedMessage}"
                return@launchWithLoading
            }

            val importResult = importer.import(backupResult.getOrThrow())
            result = buildString {
                appendLine("✅ Import complete:")
                appendLine("• Pantry items added: ${importResult.pantryItems}")
                appendLine("• Recipes added: ${importResult.recipes}")
                appendLine("• Categories added: ${importResult.categories}")
                appendLine("• Recipe refs added: ${importResult.refs}")
                appendLine("• Shopping lists added: ${importResult.lists}")
                appendLine("• Shopping entries added: ${importResult.entries}")
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
            categories = db.categoryDao().getAllOnce()
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
}