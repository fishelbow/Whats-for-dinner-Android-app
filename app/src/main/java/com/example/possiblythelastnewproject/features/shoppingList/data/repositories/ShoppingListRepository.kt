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

    suspend fun insertShoppingItems(items: List<ShoppingListItem>) {
        items.forEach { dao.insertShoppingItem(it) }
    }

    suspend fun updateShoppingItem(item: ShoppingListItem) =
        dao.updateShoppingItem(item)

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