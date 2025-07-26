package com.example.possiblythelastnewproject.debug

import android.content.Context
import android.util.Log
import com.example.possiblythelastnewproject.features.pantry.data.dao.CategoryDao
import com.example.possiblythelastnewproject.features.pantry.data.dao.PantryItemDao
import com.example.possiblythelastnewproject.features.recipe.data.dao.RecipeDao
import com.example.possiblythelastnewproject.features.recipe.data.dao.RecipePantryItemDao
import com.example.possiblythelastnewproject.features.shoppingList.data.dao.RecipeSelectionDao
import com.example.possiblythelastnewproject.features.shoppingList.data.dao.ShoppingListDao
import com.example.possiblythelastnewproject.features.shoppingList.data.dao.ShoppingListEntryDao
import com.example.possiblythelastnewproject.features.shoppingList.data.dao.ShoppingListItemDao
import com.example.possiblythelastnewproject.features.shoppingList.data.dao.UndoDao
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
        val exempt = listOf("profileInstalled") // Preserve known files
        val baseDir = context.filesDir

        val allFiles = baseDir.listFiles()
        if (allFiles.isNullOrEmpty()) {
            Log.i("ImageCleanup", "📁 No files found in ${baseDir.absolutePath}")
            return
        }

        var deletedCount = 0
        var keptCount = 0

        allFiles.forEach { file ->
            if (file.isFile && file.name !in exempt) {
                val deleted = file.delete()
                if (deleted) {
                    deletedCount++
                    Log.d("ImageCleanup", "🗑️ Deleted ${file.name}")
                } else {
                    Log.w("ImageCleanup", "⚠️ Failed to delete ${file.name}")
                }
            } else {
                keptCount++
                Log.d("ImageCleanup", "🛡️ Preserved ${file.name}")
            }
        }

        Log.i("ImageCleanup", "✅ Image cleanup finished: $deletedCount deleted, $keptCount preserved")
    }


}

