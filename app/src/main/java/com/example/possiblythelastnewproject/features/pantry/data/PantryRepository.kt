package com.example.possiblythelastnewproject.features.pantry.data

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

    suspend fun update(item: PantryItem) =
        pantryItemDao.updatePantryItem(item)

    /** Deletes unconditionally */
    suspend fun delete(item: PantryItem) =
        pantryItemDao.deletePantryItem(item)

    suspend fun findByScanCode(code: String): PantryItem? =
        pantryItemDao.getByScanCode(code)

    /** Returns true only if no recipe cross-refs exist */
    suspend fun canDelete(itemId: Long): Boolean =
        pantryItemDao.countRecipesUsing(itemId) == 0

    /** Deletes if and only if there are no recipe cross-refs */
    suspend fun deleteIfUnused(item: PantryItem): Boolean =
        if (canDelete(item.id)) {
            pantryItemDao.deletePantryItem(item)
            true
        } else {
            false
        }

    /**
     * A live Flow of every RecipePantryItemCrossRef in the DB.
     * Use this to see all pantryItemIds currently referenced by recipes.
     */
    fun observeAllCrossRefs(): Flow<List<RecipePantryItemCrossRef>> =
        recipePantryItemRepo.observeAllCrossRefs()

    /**
     * A live Flow of all pantryItemIds that are in use (as a Set<Long>).
     * Handy for disabling “Delete” buttons in your UI.
     */
    fun observeInUsePantryItemIds(): Flow<Set<Long>> =
        observeAllCrossRefs().map { list ->
            list.map { it.pantryItemId }.toSet()
        }
}