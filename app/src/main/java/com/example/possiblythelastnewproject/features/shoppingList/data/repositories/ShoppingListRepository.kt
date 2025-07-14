package com.example.possiblythelastnewproject.features.shoppingList.data.repositories

import androidx.room.Transaction
import com.example.possiblythelastnewproject.features.recipe.data.dao.RecipeDao
import com.example.possiblythelastnewproject.features.shoppingList.data.dao.ShoppingListDao
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.ShoppingList
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.ShoppingListItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingListRepository @Inject constructor(
    private val dao: ShoppingListDao,
    private val recipeDao: RecipeDao
) {

    fun getShoppingItemsForList(listId: Long): Flow<List<ShoppingListItem>> =
        dao.getShoppingItemsForList(listId)

    suspend fun upsertShoppingItem(item: ShoppingListItem) {
        val existing = dao.findItemByNameAndList(item.name, item.listId)
        if (existing != null) {
            val updated = existing.copy(quantity = item.quantity)
            dao.updateShoppingItem(updated)
        } else {
            dao.insertShoppingItem(item)
        }
    }

    suspend fun insertShoppingItems(items: List<ShoppingListItem>) {
        items.forEach { dao.insertShoppingItem(it) }
    }

    suspend fun updateShoppingItem(item: ShoppingListItem) =
        dao.updateShoppingItem(item)

    suspend fun deleteShoppingItem(item: ShoppingListItem) =
        dao.deleteShoppingItem(item)

    suspend fun clearCheckedItemsInList(listId: Long) =
        dao.clearCheckedItemsInList(listId)

    suspend fun insertShoppingList(list: ShoppingList): Long =
        dao.insertShoppingList(list)

    fun getAllShoppingLists(): Flow<List<ShoppingList>> =
        dao.getAllShoppingLists()

    suspend fun deleteGeneratedItemsInList(listId: Long) =
        dao.deleteGeneratedItemsInList(listId)

    @Transaction
    suspend fun deleteShoppingListWithItems(list: ShoppingList) {
        dao.deleteAllItemsInList(list.id)
        dao.deleteShoppingList(list)
    }

}