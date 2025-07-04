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
import kotlinx.serialization.decodeFromString
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
            result = " Exported ${backup.pantryItems.size} pantry items, ${backup.recipes.size} recipes"
        }
    }

    fun importJson(sourceUri: Uri) {
        launchWithLoading {
            val json = context.contentResolver.openInputStream(sourceUri)
                ?.bufferedReader()
                ?.readText()

            if (json == null) {
                result = " Failed to read file"
                return@launchWithLoading
            }

            val backup = Json.decodeFromString<FullDatabaseBackup>(json)

            if (backup.version > backupVersion) {
                result = " Unsupported backup version: ${backup.version}"
                return@launchWithLoading
            }

            db.clearAllTables()

            with(db) {
                categoryDao().insertAll(backup.categories)
                pantryItemDao().insertAll(backup.pantryItems)
                recipeDao().insertAll(backup.recipes)
                recipePantryItemDao().insertAll(backup.recipePantryRefs)
                shoppingListDao().insertAll(backup.shoppingLists)
                shoppingListEntryDao().insertAll(backup.shoppingListItems)
            }

            result = " Imported ${backup.pantryItems.size} pantry items, ${backup.recipes.size} recipes"
        }
    }

    private suspend fun buildBackup(): FullDatabaseBackup {
        val pantryItems = db.pantryItemDao().getAllOnce()
        val recipes = db.recipeDao().getAllOnce()
        val recipePantryRefs = db.recipePantryItemDao().getAllOnce()
        val shoppingLists = db.shoppingListDao().getAllOnce()
        val shoppingListItems = db.shoppingListEntryDao().getAllOnce()
        val categories = db.categoryDao().getAllOnce()

        println(" Exporting backup:")
        println("• Pantry items: ${pantryItems.size}")
        println("• Recipes: ${recipes.size}")
        println("• Recipe refs: ${recipePantryRefs.size}")
        println("• Shopping lists: ${shoppingLists.size}")
        println("• Shopping items: ${shoppingListItems.size}")
        println("• Categories: ${categories.size}")

        return FullDatabaseBackup(
            version = backupVersion,
            pantryItems = pantryItems,
            recipes = recipes,
            recipePantryRefs = recipePantryRefs,
            shoppingLists = shoppingLists,
            shoppingListItems = shoppingListItems,
            categories = categories
        )
    }

    private fun launchWithLoading(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            try {
                block()
            } catch (e: Exception) {
                result = " Error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}