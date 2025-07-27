package com.example.possiblythelastnewproject.backup.di

import com.example.possiblythelastnewproject.backup.ui.viewModel.FullDatabaseBackup
import com.example.possiblythelastnewproject.core.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object FullDatabaseBackupFactory {
    suspend fun createFrom(db: AppDatabase): FullDatabaseBackup = withContext(Dispatchers.IO) {
        FullDatabaseBackup(
            version = 1,
            pantryItems = db.pantryItemDao().getAllOnce(),
            recipes = db.recipeDao().getAllOnce(),
            recipePantryRefs = db.recipePantryItemDao().getAllOnce(),
            shoppingLists = db.shoppingListDao().getAllOnce(),
            shoppingListItems = db.shoppingListEntryDao().getAllOnce(),
            recipeSelections = db.recipeSelectionDao().getAllOnce(),
            undoActions = db.undoDao().getAllOnce(),
            categories = db.categoryDao().getAllOnce()
        )
    }
}