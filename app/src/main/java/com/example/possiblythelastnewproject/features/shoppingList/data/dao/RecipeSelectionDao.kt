package com.example.possiblythelastnewproject.features.shoppingList.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.RecipeSelection
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeSelectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(selection: RecipeSelection)

    @Query("SELECT * FROM RecipeSelection WHERE listId = :listId")
    suspend fun getSelectionsForList(listId: Long): List<RecipeSelection>

    @Query("DELETE FROM RecipeSelection WHERE listId = :listId AND recipeId = :recipeId")
    suspend fun deleteSelection(listId: Long, recipeId: Long)

    @Query("DELETE FROM RecipeSelection WHERE listId = :listId")
    suspend fun deleteAllForList(listId: Long)

    @Query("SELECT * FROM RecipeSelection WHERE listId = :listId")
    fun getSelectionsFlow(listId: Long): Flow<List<RecipeSelection>>

    @Query("DELETE FROM RecipeSelection")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(selections: List<RecipeSelection>)

    @Query("SELECT * FROM RecipeSelection")
    suspend fun getAllOnce(): List<RecipeSelection>

    @Query("SELECT * FROM RecipeSelection ORDER BY listId LIMIT :limit OFFSET :offset")
    suspend fun getPaged(limit: Int, offset: Int): List<RecipeSelection>
}
