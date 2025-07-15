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

}
