package com.example.possiblythelastnewproject.core.data.csvBackup

import com.example.possiblythelastnewproject.features.pantry.data.entities.Category
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.recipe.data.entities.Recipe
import com.example.possiblythelastnewproject.features.recipe.data.entities.RecipePantryItemCrossRef
import com.example.possiblythelastnewproject.features.shoppingList.data.entity.ShoppingList
import com.example.possiblythelastnewproject.features.shoppingList.data.entity.ShoppingListItem

data class ParsedBackup(
    val pantryItems: List<PantryItem> = emptyList(),
    val shoppingLists: List<ShoppingList> = emptyList(),
    val shoppingListItems: List<ShoppingListItem> = emptyList(),
    val recipes: List<Recipe> = emptyList(),
    val recipePantryRefs: List<RecipePantryItemCrossRef> = emptyList(),
    val categories: List<Category> = emptyList()
)