package com.example.possiblythelastnewproject.features.shoppingList.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.UndoAction

@Dao
interface UndoDao {

    @Insert
    suspend fun insert(action: UndoAction)

    @Query("SELECT * FROM UndoAction WHERE listId = :listId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastAction(listId: Long): UndoAction?

    @Delete
    suspend fun delete(action: UndoAction)

    @Query("DELETE FROM UndoAction WHERE listId = :listId")
    suspend fun clearUndoHistory(listId: Long)

    @Query("DELETE FROM RecipeSelection")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(actions: List<UndoAction>)


    @Query("SELECT * FROM UndoAction")
    suspend fun getAllOnce(): List<UndoAction>

    @Query("SELECT * FROM UndoAction ORDER BY listId, timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getPaged(limit: Int, offset: Int): List<UndoAction>
}