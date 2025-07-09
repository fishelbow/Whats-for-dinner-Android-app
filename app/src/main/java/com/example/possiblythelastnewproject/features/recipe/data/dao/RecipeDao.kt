package com.example.possiblythelastnewproject.features.recipe.data.dao

import androidx.room.*
import com.example.possiblythelastnewproject.features.recipe.data.RecipeWithIngredients
import com.example.possiblythelastnewproject.features.recipe.data.entities.Recipe
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT EXISTS(SELECT 1 FROM Recipe WHERE LOWER(recipe_name) = LOWER(:name))")
    suspend fun existsByName(name: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM Recipe WHERE LOWER(recipe_name) = LOWER(:name) AND uuid != :excludeUuid)")
    suspend fun existsByNameExcludingUuid(name: String, excludeUuid: String): Boolean

    @Query("SELECT * FROM Recipe ORDER BY recipe_name")
    fun getAllRecipes(): Flow<List<Recipe>>

    @Query("SELECT * FROM Recipe WHERE id = :recipeId")
    suspend fun getRecipeById(recipeId: Long): Recipe?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe): Long

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    @Transaction
    @Query("SELECT * FROM Recipe ORDER BY recipe_name")
    fun getAllRecipesWithIngredients(): Flow<List<RecipeWithIngredients>>

    @Transaction
    @Query("SELECT * FROM Recipe WHERE id = :id")
    suspend fun getRecipeWithIngredients(id: Long): RecipeWithIngredients?

    @Query("DELETE FROM Recipe WHERE id = :id")
    suspend fun deleteRecipeById(id: Long)

    @Query("SELECT * FROM Recipe")
    suspend fun getAllOnce(): List<Recipe>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Recipe>)


    @Query("DELETE FROM recipe")
    suspend fun clearAll()

}