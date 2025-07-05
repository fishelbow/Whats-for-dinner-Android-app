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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class DbBackupViewModel @Inject constructor(
    private val db: AppDatabase,
    private val context: Application
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var result: String? by mutableStateOf(null)
        private set

    private val backupVersion = 1

    fun clearResult() {
        result = null
    }

    fun exportJson(destinationUri: Uri) {
        launchWithLoading {
            val backup = FullDatabaseBackup(
                version = backupVersion,
                pantryItems = db.pantryItemDao().getAllOnce(),
                recipes = db.recipeDao().getAllOnce(),
                recipePantryRefs = db.recipePantryItemDao().getAllOnce(),
                shoppingLists = db.shoppingListDao().getAllOnce(),
                shoppingListItems = db.shoppingListEntryDao().getAllOnce(),
                categories = db.categoryDao().getAllOnce()
            )

            val json = Json.encodeToString(backup)
            context.contentResolver.openOutputStream(destinationUri)?.use {
                it.write(json.toByteArray())
            } ?: run {
                result = "❌ Failed to open output stream"
                return@launchWithLoading
            }

            result = "✅ Exported ${backup.pantryItems.size} pantry items, ${backup.recipes.size} recipes"
        }
    }

    fun importJson(sourceUri: Uri) {
        launchWithLoading {
            val json = context.contentResolver.openInputStream(sourceUri)
                ?.bufferedReader()
                ?.readText()

            if (json == null) {
                result = "❌ Failed to read file"
                return@launchWithLoading
            }

            val backup = try {
                Json.decodeFromString<FullDatabaseBackup>(json)
            } catch (e: Exception) {
                result = "❌ Failed to parse backup: ${e.localizedMessage}"
                return@launchWithLoading
            }

            if (backup.version > backupVersion) {
                result = "❌ Unsupported backup version: ${backup.version}"
                return@launchWithLoading
            }

            mergeBackupIntoDatabase(backup)
        }
    }

    private suspend fun mergeBackupIntoDatabase(backup: FullDatabaseBackup) {
        with(db) {
            val newCategories = filterNew(backup.categories, categoryDao().getAllOnce()) { it.uuid }
            val newPantryItems = filterNew(backup.pantryItems, pantryItemDao().getAllOnce()) { it.uuid }
            val newRecipes = filterNew(backup.recipes, recipeDao().getAllOnce()) { it.uuid }
            val newRefs = filterNew(backup.recipePantryRefs, recipePantryItemDao().getAllOnce()) { it.uuid }
            val newLists = filterNew(backup.shoppingLists, shoppingListDao().getAllOnce()) { it.uuid }
            val newEntries = filterNew(backup.shoppingListItems, shoppingListEntryDao().getAllOnce()) { it.uuid }

            categoryDao().insertAll(newCategories)
            pantryItemDao().insertAll(newPantryItems)
            recipeDao().insertAll(newRecipes)
            recipePantryItemDao().insertAll(newRefs)
            shoppingListDao().insertAll(newLists)
            shoppingListEntryDao().insertAll(newEntries)

            result = buildString {
                appendLine("✅ Import complete:")
                appendLine("• Pantry items added: ${newPantryItems.size}")
                appendLine("• Recipes added: ${newRecipes.size}")
                appendLine("• Categories added: ${newCategories.size}")
                appendLine("• Recipe refs added: ${newRefs.size}")
                appendLine("• Shopping lists added: ${newLists.size}")
                appendLine("• Shopping entries added: ${newEntries.size}")
            }
        }
    }

    private fun <T> filterNew(newItems: List<T>, existingItems: List<T>, keySelector: (T) -> String): List<T> {
        val existingKeys = existingItems.map(keySelector).toSet()
        return newItems.filterNot { keySelector(it) in existingKeys }
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