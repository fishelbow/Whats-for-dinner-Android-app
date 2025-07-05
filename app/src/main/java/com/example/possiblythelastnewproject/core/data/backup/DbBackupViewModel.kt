package com.example.possiblythelastnewproject.core.data.backup

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.possiblythelastnewproject.core.data.AppDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@Serializable
data class BackupWrapper(
    val type: String = "my_app_backup",
    val version: Int = 1,
    val payload: FullDatabaseBackup
)

@HiltViewModel
class DbBackupViewModel @Inject constructor(
    private val db: AppDatabase,
    private val context: Application
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var result: String? by mutableStateOf(null)
        private set

    private val currentVersion = 1

    fun clearResult() {
        result = null
    }

    fun exportJson(destinationUri: Uri) {
        launchWithLoading {
            val backup = FullDatabaseBackup(
                version = currentVersion,
                pantryItems = db.pantryItemDao().getAllOnce(),
                recipes = db.recipeDao().getAllOnce(),
                recipePantryRefs = db.recipePantryItemDao().getAllOnce(),
                shoppingLists = db.shoppingListDao().getAllOnce(),
                shoppingListItems = db.shoppingListEntryDao().getAllOnce(),
                categories = db.categoryDao().getAllOnce()
            )

            val wrapper = BackupWrapper(
                version = currentVersion,
                payload = backup
            )

            val json = Json.encodeToString(wrapper)
            context.contentResolver.openOutputStream(destinationUri)?.use {
                it.write(json.toByteArray())
            }

            result = "✅ Exported backup with ${backup.pantryItems.size} pantry items, ${backup.recipes.size} recipes"
        }
    }

    fun importJson(sourceUri: Uri) {
        launchWithLoading {
            val jsonText = context.contentResolver.openInputStream(sourceUri)
                ?.bufferedReader()
                ?.readText()

            if (jsonText.isNullOrBlank()) {
                result = "❌ File is empty or unreadable"
                return@launchWithLoading
            }

            val wrapper = try {
                Json.decodeFromString<BackupWrapper>(jsonText)
            } catch (e: Exception) {
                result = "❌ Failed to parse backup: ${e.localizedMessage}"
                return@launchWithLoading
            }

            if (wrapper.type != "my_app_backup") {
                result = "❌ Invalid backup file type"
                return@launchWithLoading
            }

            if (wrapper.version > currentVersion) {
                result = "❌ Unsupported backup version: ${wrapper.version}"
                return@launchWithLoading
            }

            val backup = wrapper.payload

            db.categoryDao().insertAll(backup.categories)
            db.pantryItemDao().insertAll(backup.pantryItems)
            db.recipeDao().insertAll(backup.recipes)
            db.recipePantryItemDao().insertAll(backup.recipePantryRefs)
            db.shoppingListDao().insertAll(backup.shoppingLists)
            db.shoppingListEntryDao().insertAll(backup.shoppingListItems)

            result = buildString {
                appendLine("✅ Import complete:")
                appendLine("• Pantry items: ${backup.pantryItems.size}")
                appendLine("• Recipes: ${backup.recipes.size}")
                appendLine("• Categories: ${backup.categories.size}")
                appendLine("• Recipe refs: ${backup.recipePantryRefs.size}")
                appendLine("• Shopping lists: ${backup.shoppingLists.size}")
                appendLine("• Shopping entries: ${backup.shoppingListItems.size}")
            }
        }
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