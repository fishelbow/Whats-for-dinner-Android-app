package com.example.possiblythelastnewproject.features.shoppingList.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingListRepository @Inject constructor(
    private val shoppingListDao: ShoppingListDao
) {
    fun getAllItems(): Flow<List<ShoppingListItem>> = shoppingListDao.getAllShoppingItems()

    suspend fun insert(item: ShoppingListItem) = shoppingListDao.insertItem(item)

    suspend fun update(item: ShoppingListItem) = shoppingListDao.updateItem(item)

    suspend fun delete(item: ShoppingListItem) = shoppingListDao.deleteItem(item)

    suspend fun clearChecked() = shoppingListDao.clearCheckedItems()
}