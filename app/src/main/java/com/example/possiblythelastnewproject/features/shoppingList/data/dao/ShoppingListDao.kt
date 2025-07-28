package com.example.possiblythelastnewproject.features.shoppingList.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.ShoppingList
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: ShoppingList): Long

    @Query("SELECT * FROM ShoppingList ORDER BY createdAt DESC")
    suspend fun getAllLists(): List<ShoppingList>

    @Query("DELETE FROM ShoppingList WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM ShoppingList ORDER BY createdAt DESC")
    fun getAllListsFlow(): Flow<List<ShoppingList>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(lists: List<ShoppingList>)

    @Query("SELECT * FROM ShoppingList")
    suspend fun getAllOnce(): List<ShoppingList>

    @Query("DELETE FROM ShoppingList")
    suspend fun clearAll()

    @Query("SELECT * FROM ShoppingList ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getPaged(limit: Int, offset: Int): List<ShoppingList>

}