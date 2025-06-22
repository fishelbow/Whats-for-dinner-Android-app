package com.example.possiblythelastnewproject.features.recipe.data.repository

import com.example.possiblythelastnewproject.features.recipe.data.dao.RecipePantryItemDao
import com.example.possiblythelastnewproject.features.recipe.data.entities.RecipePantryItemCrossRef
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipePantryItemRepository @Inject constructor(
    private val dao: RecipePantryItemDao
) {

    /** Live stream of every link in the DB */
    fun observeAllCrossRefs(): Flow<List<RecipePantryItemCrossRef>> =
        dao.getAllCrossRefs()

    /** Live stream of links for a single recipe */
    fun observeCrossRefsForRecipe(recipeId: Long): Flow<List<RecipePantryItemCrossRef>> =
        dao.getCrossRefsForRecipeFlow(recipeId)

    /** One-off fetch of links for a recipe */
    suspend fun getCrossRefsForRecipeOnce(recipeId: Long): List<RecipePantryItemCrossRef> =
        dao.getCrossRefsForRecipeOnce(recipeId)

    /** Insert or replace a single link */
    suspend fun insertCrossRef(ref: RecipePantryItemCrossRef) =
        dao.insertCrossRef(ref)

    /** Delete a single link by entity */
    suspend fun deleteCrossRef(ref: RecipePantryItemCrossRef) =
        dao.deleteCrossRef(ref)

    /** Clear all links for a recipe */
    suspend fun deleteCrossRefsForRecipe(recipeId: Long) =
        dao.deleteCrossRefsForRecipe(recipeId)

    /**
     * Atomically replace all links for a recipe:
     *   1) delete existing
     *   2) insert each new one
     */
    suspend fun replaceIngredientsForRecipe(
        recipeId: Long,
        newRefs: List<RecipePantryItemCrossRef>
    ) {
        // Step 1: Remove old links
        dao.deleteCrossRefsForRecipe(recipeId)

        // Step 2: Insert each new cross-ref
        newRefs.forEach { ref ->
            dao.insertCrossRef(ref)
        }
    }
}