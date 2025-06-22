package com.example.possiblythelastnewproject.features.shoppingList.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {

    @Query("SELECT * FROM ShoppingListItem ORDER BY name")
    fun getAllShoppingItems(): Flow<List<ShoppingListItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingListItem)

    @Update
    suspend fun updateItem(item: ShoppingListItem)

    @Delete
    suspend fun deleteItem(item: ShoppingListItem)

    @Query("DELETE FROM ShoppingListItem WHERE isChecked = 1")
    suspend fun clearCheckedItems()
}