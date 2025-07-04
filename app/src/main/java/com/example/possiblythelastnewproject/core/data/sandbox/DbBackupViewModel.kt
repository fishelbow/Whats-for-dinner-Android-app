package com.example.possiblythelastnewproject.core.data.sandbox

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
            val backup = buildBackup()
            val json = Json.encodeToString(backup)
            context.contentResolver.openOutputStream(destinationUri)?.use {
                it.write(json.toByteArray())
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

            with(db) {
                val existingPantryUuids = pantryItemDao().getAllOnce().map { it.uuid }.toSet()
                val newPantryItems = backup.pantryItems.filterNot { it.uuid in existingPantryUuids }

                val existingRecipeUuids = recipeDao().getAllOnce().map { it.uuid }.toSet()
                val newRecipes = backup.recipes.filterNot { it.uuid in existingRecipeUuids }

                val existingCategoryUuids = categoryDao().getAllOnce().map { it.uuid }.toSet()
                val newCategories = backup.categories.filterNot { it.uuid in existingCategoryUuids }

                val existingRefUuids = recipePantryItemDao().getAllOnce().map { it.uuid }.toSet()
                val newRefs = backup.recipePantryRefs.filterNot { it.uuid in existingRefUuids }

                val existingListUuids = shoppingListDao().getAllOnce().map { it.uuid }.toSet()
                val newLists = backup.shoppingLists.filterNot { it.uuid in existingListUuids }

                val existingEntryUuids = shoppingListEntryDao().getAllOnce().map { it.uuid }.toSet()
                val newEntries = backup.shoppingListItems.filterNot { it.uuid in existingEntryUuids }

                // Insert only new items
                if (newCategories.isNotEmpty()) categoryDao().insertAll(newCategories)
                if (newPantryItems.isNotEmpty()) pantryItemDao().insertAll(newPantryItems)
                if (newRecipes.isNotEmpty()) recipeDao().insertAll(newRecipes)
                if (newRefs.isNotEmpty()) recipePantryItemDao().insertAll(newRefs)
                if (newLists.isNotEmpty()) shoppingListDao().insertAll(newLists)
                if (newEntries.isNotEmpty()) shoppingListEntryDao().insertAll(newEntries)

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
    }
    private suspend fun buildBackup(): FullDatabaseBackup {
        return FullDatabaseBackup(
            version = backupVersion,
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