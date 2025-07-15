package com.example.possiblythelastnewproject.features.shoppingList.data.dao

import androidx.room.*
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.ShoppingListItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ShoppingListItem)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<ShoppingListItem>)

    @Query("SELECT * FROM ShoppingListItem WHERE listId = :listId")
    fun getItemsForList(listId: Long): Flow<List<ShoppingListItem>>

    @Query("SELECT * FROM ShoppingListItem")
    suspend fun getAllOnce(): List<ShoppingListItem>

    @Query("DELETE FROM ShoppingListItem WHERE listId = :listId AND isGenerated = 1")
    suspend fun deleteGeneratedItems(listId: Long)

    @Query("DELETE FROM ShoppingListItem WHERE listId = :listId")
    suspend fun deleteAllFromList(listId: Long)

    @Query("UPDATE ShoppingListItem SET isChecked = :checked WHERE uuid = :uuid")
    suspend fun setFoundStatus(uuid: String, checked: Boolean)

    @Query("DELETE FROM ShoppingListItem WHERE uuid = :uuid")
    suspend fun deleteByUuid(uuid: String)

    @Query("SELECT * FROM ShoppingListItem WHERE listId = :listId")
    suspend fun getItemsForListOnce(listId: Long): List<ShoppingListItem>
}