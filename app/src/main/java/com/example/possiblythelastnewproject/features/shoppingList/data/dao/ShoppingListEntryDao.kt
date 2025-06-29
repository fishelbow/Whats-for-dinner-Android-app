package com.example.possiblythelastnewproject.features.shoppingList.data.dao

import androidx.room.*
import com.example.possiblythelastnewproject.features.shoppingList.data.entity.ShoppingList
import com.example.possiblythelastnewproject.features.shoppingList.data.entity.ShoppingListItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListEntryDao {

    @Insert
    suspend fun insert(list: ShoppingList): Long

    @Delete
    suspend fun delete(list: ShoppingList)

    @Query("SELECT * FROM ShoppingList ORDER BY createdAt DESC")
    fun getAllLists(): Flow<List<ShoppingList>>

    @Query("SELECT * FROM ShoppingList WHERE id = :listId LIMIT 1")
    suspend fun getById(listId: Long): ShoppingList?

    @Query("SELECT * FROM ShoppingList")
    suspend fun getAllOnce(): List<ShoppingList>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ShoppingListItem>)


}