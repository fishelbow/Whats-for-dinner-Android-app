package com.example.possiblythelastnewproject.features.shoppingList.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.ShoppingList
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.ShoppingListItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {

    @Query("SELECT * FROM ShoppingListItem ORDER BY name")
    fun getAllShoppingItems(): Flow<List<ShoppingListItem>>

    @Query("SELECT * FROM ShoppingListItem WHERE listId = :listId ORDER BY name")
    fun getShoppingItemsForList(listId: Long): Flow<List<ShoppingListItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItem(item: ShoppingListItem)

    @Update
    suspend fun updateShoppingItem(item: ShoppingListItem)

    @Delete
    suspend fun deleteShoppingItem(item: ShoppingListItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingList(shoppingList: ShoppingList): Long

    @Delete
    suspend fun deleteShoppingList(shoppingList: ShoppingList)

    @Query("SELECT * FROM ShoppingList ORDER BY createdAt DESC")
    fun getAllShoppingLists(): Flow<List<ShoppingList>>

    @Query("DELETE FROM ShoppingListItem WHERE isChecked = 1 AND listId = :listId")
    suspend fun clearCheckedItemsInList(listId: Long)

    @Query("DELETE FROM ShoppingListItem WHERE listId = :listId AND isGenerated = 1")
    suspend fun deleteGeneratedItemsInList(listId: Long)

    @Query("DELETE FROM ShoppingListItem WHERE listId = :listId")
    suspend fun deleteAllItemsInList(listId: Long)


    @Query("SELECT * FROM ShoppingList")
    suspend fun getAllOnce(): List<ShoppingList>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ShoppingList>)


}