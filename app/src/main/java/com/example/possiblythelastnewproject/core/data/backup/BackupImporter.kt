package com.example.possiblythelastnewproject.core.data.backup

import com.example.possiblythelastnewproject.core.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BackupImporter @Inject constructor(
    private val db: AppDatabase
) {
    suspend fun import(backup: FullDatabaseBackup): ImportResult = withContext(Dispatchers.IO) {
        val pantry = db.pantryItemDao().getAllOnce().map { it.uuid }.toSet()
        val recipes = db.recipeDao().getAllOnce().map { it.uuid }.toSet()
        val categories = db.categoryDao().getAllOnce().map { it.uuid }.toSet()
        val refs = db.recipePantryItemDao().getAllOnce().map { it.uuid }.toSet()
        val lists = db.shoppingListDao().getAllOnce().map { it.uuid }.toSet()
        val entries = db.shoppingListEntryDao().getAllOnce().map { it.uuid }.toSet()

        val newPantry = backup.pantryItems.filterNot { it.uuid in pantry }
        val newRecipes = backup.recipes.filterNot { it.uuid in recipes }
        val newCategories = backup.categories.filterNot { it.uuid in categories }
        val newRefs = backup.recipePantryRefs.filterNot { it.uuid in refs }
        val newLists = backup.shoppingLists.filterNot { it.uuid in lists }
        val newEntries = backup.shoppingListItems.filterNot { it.uuid in entries }

        if (newCategories.isNotEmpty()) db.categoryDao().insertAll(newCategories)
        if (newPantry.isNotEmpty()) db.pantryItemDao().insertAll(newPantry)
        if (newRecipes.isNotEmpty()) db.recipeDao().insertAll(newRecipes)
        if (newRefs.isNotEmpty()) db.recipePantryItemDao().insertAll(newRefs)
        if (newLists.isNotEmpty()) db.shoppingListDao().insertAll(newLists)
        if (newEntries.isNotEmpty()) db.shoppingListEntryDao().insertAll(newEntries)

        ImportResult(
            pantryItems = newPantry.size,
            recipes = newRecipes.size,
            categories = newCategories.size,
            refs = newRefs.size,
            lists = newLists.size,
            entries = newEntries.size
        )
    }
}

data class ImportResult(
    val pantryItems: Int,
    val recipes: Int,
    val categories: Int,
    val refs: Int,
    val lists: Int,
    val entries: Int
)