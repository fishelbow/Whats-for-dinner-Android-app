package com.example.possiblythelastnewproject.features.shoppingList.ui.model

import com.example.possiblythelastnewproject.features.shoppingList.data.entity.ShoppingListItem

data class CategorizedShoppingItem(
    val item: ShoppingListItem,
    val category: String
)