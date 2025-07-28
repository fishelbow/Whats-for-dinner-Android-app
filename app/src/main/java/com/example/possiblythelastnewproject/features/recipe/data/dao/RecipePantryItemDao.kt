package com.example.possiblythelastnewproject.features.recipe.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.possiblythelastnewproject.features.recipe.data.entities.RecipePantryItemCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipePantryItemDao {

    /** Insert or replace a single recipe⇄pantry cross-ref. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: RecipePantryItemCrossRef)

    /** Insert or replace multiple cross-refs. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(refs: List<RecipePantryItemCrossRef>)

    /** Delete a specific cross-ref by entity. */
    @Delete
    suspend fun deleteCrossRef(crossRef: RecipePantryItemCrossRef)

    /** Delete a specific cross-ref by pantryItemId + recipeId. */
    @Query("""
        DELETE FROM RecipePantryItemCrossRef
        WHERE recipeId = :recipeId AND pantryItemId = :pantryItemId
    """)
    suspend fun deleteCrossRef(pantryItemId: Long, recipeId: Long)

    /** Delete all pantry-links for a single recipe. */
    @Query("DELETE FROM RecipePantryItemCrossRef WHERE recipeId = :recipeId")
    suspend fun deleteCrossRefsForRecipe(recipeId: Long)

    /** One-off fetch of all cross-refs for a recipe. */
    @Query("SELECT * FROM RecipePantryItemCrossRef WHERE recipeId = :recipeId")
    suspend fun getCrossRefsForRecipeOnce(recipeId: Long): List<RecipePantryItemCrossRef>

    /** Live stream of all cross-refs for a recipe. */
    @Query("SELECT * FROM RecipePantryItemCrossRef WHERE recipeId = :recipeId")
    fun getCrossRefsForRecipeFlow(recipeId: Long): Flow<List<RecipePantryItemCrossRef>>

    /** Live stream of every cross-ref in the DB. */
    @Query("SELECT * FROM RecipePantryItemCrossRef")
    fun getAllCrossRefs(): Flow<List<RecipePantryItemCrossRef>>

    /** One-off fetch of all cross-refs. */
    @Query("SELECT * FROM RecipePantryItemCrossRef")
    suspend fun getAllOnce(): List<RecipePantryItemCrossRef>

    @Query("DELETE FROM RecipePantryItemCrossRef")
    suspend fun clearAll()

    @Query("SELECT * FROM RecipePantryItemCrossRef LIMIT :limit OFFSET :offset")
    suspend fun getPaged(limit: Int, offset: Int): List<RecipePantryItemCrossRef>
}
