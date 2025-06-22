package com.example.possiblythelastnewproject.features.recipe.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.possiblythelastnewproject.features.recipe.data.RecipeWithIngredients
import com.example.possiblythelastnewproject.features.recipe.data.entities.Recipe
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

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


}