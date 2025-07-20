package com.example.possiblythelastnewproject.features.pantry.data

import android.content.Context
import com.example.possiblythelastnewproject.core.utils.deleteImageFromStorage
import com.example.possiblythelastnewproject.features.pantry.data.dao.CategoryDao
import com.example.possiblythelastnewproject.features.pantry.data.dao.PantryItemDao
import com.example.possiblythelastnewproject.features.pantry.data.entities.Category
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.recipe.data.entities.RecipePantryItemCrossRef
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipePantryItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PantryRepository @Inject constructor(
    private val pantryItemDao: PantryItemDao,
    private val recipePantryItemRepo: RecipePantryItemRepository,
    private val categoryDao: CategoryDao,
    private val defaultCategoryNames: List<String> = listOf(
        "Grains", "Vegetables", "Fruits", "Dairy", "Proteins", "Snacks", "Spices"
    )
) {

    suspend fun populateDefaultCategoriesIfEmpty() {
        val current = categoryDao.getAllCategoriesOnce()
        if (current.isEmpty()) {
            defaultCategoryNames.forEach { name ->
                categoryDao.insertCategory(Category(name = name))
            }
        }
    }

    fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories()
    }

    fun getAllPantryItems(): Flow<List<PantryItem>> =
        pantryItemDao.getAllPantryItems()

    suspend fun insert(item: PantryItem) =
        pantryItemDao.insertPantryItem(item)

    suspend fun update(item: PantryItem, oldImageUri: String, context: Context) {
        if (item.imageUri != oldImageUri) {
            deleteImageFromStorage(oldImageUri, context)
        }
        pantryItemDao.updatePantryItem(item)
    }

    /** Deletes unconditionally */
    suspend fun delete(item: PantryItem, context: Context) {
        item.imageUri?.let { deleteImageFromStorage(it, context) }
        pantryItemDao.deletePantryItem(item)
    }

    /** Returns true only if no recipe cross-refs exist */
    private suspend fun canDelete(itemId: Long): Boolean =
        pantryItemDao.countRecipesUsing(itemId) == 0


    /**
     * A live Flow of every RecipePantryItemCrossRef in the DB.
     * Use this to see all pantryItemIds currently referenced by recipes.
     */
    fun observeAllCrossRefs(): Flow<List<RecipePantryItemCrossRef>> =
        recipePantryItemRepo.observeAllCrossRefs()


    suspend fun clearAll() {
        pantryItemDao.clearAll()
    }
}