package com.example.possiblythelastnewproject.debug

import android.content.Context
import com.example.possiblythelastnewproject.features.pantry.data.dao.CategoryDao
import com.example.possiblythelastnewproject.features.pantry.data.dao.PantryItemDao
import com.example.possiblythelastnewproject.features.recipe.data.dao.RecipeDao
import com.example.possiblythelastnewproject.features.recipe.data.dao.RecipePantryItemDao
import com.example.possiblythelastnewproject.features.shoppingList.data.dao.RecipeSelectionDao
import com.example.possiblythelastnewproject.features.shoppingList.data.dao.ShoppingListDao
import com.example.possiblythelastnewproject.features.shoppingList.data.dao.ShoppingListEntryDao
import com.example.possiblythelastnewproject.features.shoppingList.data.dao.ShoppingListItemDao
import com.example.possiblythelastnewproject.features.shoppingList.data.dao.UndoDao
import java.io.File
import javax.inject.Inject

class DebugRepository @Inject constructor(
    private val pantryItemDao: PantryItemDao,
    private val recipeDao: RecipeDao,
    private val categoryDao: CategoryDao,
    private val recipePantryItemDao: RecipePantryItemDao,
    private val shoppingListDao: ShoppingListDao,
    private val shoppingListItemDao: ShoppingListItemDao,
    private val shoppingListEntryDao: ShoppingListEntryDao,
    private val recipeSelectionDao: RecipeSelectionDao,
    private val undoDao: UndoDao,


    // and the rest...
) {
    suspend fun clearDbEntries() {

        //  Clear children first to avoid FK constraints
        recipePantryItemDao.clearAll()
        shoppingListItemDao.clearAll()
        shoppingListEntryDao.clearAll()
        recipeSelectionDao.clearAll()
        undoDao.clearAll()

        // Clear parent entities
        recipeDao.clearAll()
        pantryItemDao.clearAll()
      //  categoryDao.clearAll()
        shoppingListDao.clearAll()
    }

    fun deleteAllAppImages(context: Context) {
        val imageDir = File(context.filesDir, "images")
        if (imageDir.exists()) {
            imageDir.listFiles()?.forEach { it.delete() }
        }
    }

}

