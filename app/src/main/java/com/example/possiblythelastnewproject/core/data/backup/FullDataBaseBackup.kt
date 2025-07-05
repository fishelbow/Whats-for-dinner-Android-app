package com.example.possiblythelastnewproject.core.data.backup

import kotlinx.serialization.Serializable
import com.example.possiblythelastnewproject.features.pantry.data.entities.*
import com.example.possiblythelastnewproject.features.recipe.data.entities.*
import com.example.possiblythelastnewproject.features.shoppingList.data.entity.*

@Serializable
data class FullDatabaseBackup(
    val version: Int = 1, // default to version 1
    val pantryItems: List<PantryItem>,
    val recipes: List<Recipe>,
    val recipePantryRefs: List<RecipePantryItemCrossRef>,
    val shoppingLists: List<ShoppingList>,
    val shoppingListItems: List<ShoppingListItem>,
    val categories: List<Category>
)