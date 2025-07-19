package com.example.possiblythelastnewproject.core.data.backup

import com.example.possiblythelastnewproject.core.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BackupImporter @Inject constructor(
    private val db: AppDatabase
) {
    suspend fun import(backup: FullDatabaseBackup): ImportResult = withContext(Dispatchers.IO) {
        fun <T, ID> filterNew(
            existing: Set<ID>,
            incoming: List<T>,
            idSelector: (T) -> ID
        ): List<T> {
            return incoming.filterNot { idSelector(it) in existing }
        }

        val pantry = db.pantryItemDao().getAllOnce().map { it.uuid }.toSet()
        val recipes = db.recipeDao().getAllOnce().map { it.uuid }.toSet()
        val categories = db.categoryDao().getAllOnce().map { it.uuid }.toSet()
        val refs = db.recipePantryItemDao().getAllOnce().map { it.uuid }.toSet()
        val lists = db.shoppingListDao().getAllOnce().map { it.uuid }.toSet()
        val entries = db.shoppingListEntryDao().getAllOnce().map { it.uuid }.toSet()
        val selections = db.recipeSelectionDao().getAllOnce().map { it.id }.toSet()
        val actions = db.undoDao().getAllOnce().map { it.id }.toSet()

        val newCategories = filterNew(categories, backup.categories) { it.uuid }
        val newPantry = filterNew(pantry, backup.pantryItems) { it.uuid }
        val newRecipes = filterNew(recipes, backup.recipes) { it.uuid }
        val newRefs = filterNew(refs, backup.recipePantryRefs) { it.uuid }
        val newLists = filterNew(lists, backup.shoppingLists) { it.uuid }
        val newEntries = filterNew(entries, backup.shoppingListItems) { it.uuid }
        val newSelections = filterNew(selections, backup.recipeSelections) { it.id }
        val newActions = filterNew(actions, backup.undoActions) { it.id }

        if (newCategories.isNotEmpty()) db.categoryDao().insertAll(newCategories)
        if (newPantry.isNotEmpty()) db.pantryItemDao().insertAll(newPantry)
        if (newRecipes.isNotEmpty()) db.recipeDao().insertAll(newRecipes)
        if (newRefs.isNotEmpty()) db.recipePantryItemDao().insertAll(newRefs)
        if (newLists.isNotEmpty()) db.shoppingListDao().insertAll(newLists)
        if (newEntries.isNotEmpty()) db.shoppingListEntryDao().insertAll(newEntries)
        if (newSelections.isNotEmpty()) db.recipeSelectionDao().insertAll(newSelections)
        if (newActions.isNotEmpty()) db.undoDao().insertAll(newActions)

        ImportResult(
            pantryItems = newPantry.size,
            recipes = newRecipes.size,
            categories = newCategories.size,
            refs = newRefs.size,
            lists = newLists.size,
            entries = newEntries.size,
            selections = newSelections.size,
            undoActions = newActions.size
        )
    }
}

data class ImportResult(
    val pantryItems: Int,
    val recipes: Int,
    val categories: Int,
    val refs: Int,
    val lists: Int,
    val entries: Int,
    val selections: Int,
    val undoActions: Int
)