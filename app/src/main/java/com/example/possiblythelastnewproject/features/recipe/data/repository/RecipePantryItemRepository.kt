package com.example.possiblythelastnewproject.features.recipe.data.repository

import com.example.possiblythelastnewproject.features.pantry.data.dao.PantryItemDao
import com.example.possiblythelastnewproject.features.recipe.data.dao.RecipePantryItemDao
import com.example.possiblythelastnewproject.features.recipe.data.entities.RecipePantryItemCrossRef
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipePantryItemRepository @Inject constructor(
    private val dao: RecipePantryItemDao,
    private val pantryItemDao: PantryItemDao
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

    /** Clear all links for a recipe */
    suspend fun deleteCrossRefsForRecipe(recipeId: Long) =
        dao.deleteCrossRefsForRecipe(recipeId)

    suspend fun replaceIngredientsForRecipe(
        recipeId: Long,
        newRefs: List<RecipePantryItemCrossRef>
    ) {

        dao.deleteCrossRefsForRecipe(recipeId)


        newRefs.forEach { ref ->
            dao.insertCrossRef(ref)
        }
    }


    suspend fun clearAll() {
        dao.clearAll()
    }

}