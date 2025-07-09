package com.example.possiblythelastnewproject.features.recipe.data.repository

import com.example.possiblythelastnewproject.features.recipe.data.RecipeWithIngredients
import com.example.possiblythelastnewproject.features.recipe.data.dao.RecipeDao
import com.example.possiblythelastnewproject.features.recipe.data.entities.Recipe
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepository @Inject constructor(
    private val recipeDao: RecipeDao
) {

    suspend fun getRecipeWithIngredients(recipeId: Long): RecipeWithIngredients? =
        recipeDao.getRecipeWithIngredients(recipeId)

    suspend fun insert(recipe: Recipe): Long =
        recipeDao.insertRecipe(recipe)

    suspend fun update(recipe: Recipe) =
        recipeDao.updateRecipe(recipe)

    suspend fun delete(recipe: Recipe) =
        recipeDao.deleteRecipe(recipe)

    fun getRecipesWithIngredients(): Flow<List<RecipeWithIngredients>> =
        recipeDao.getAllRecipesWithIngredients()

    fun getAllRecipes(): Flow<List<Recipe>> =
        recipeDao.getAllRecipes()

    suspend fun clearAll() {
        recipeDao.clearAll()
    }
}