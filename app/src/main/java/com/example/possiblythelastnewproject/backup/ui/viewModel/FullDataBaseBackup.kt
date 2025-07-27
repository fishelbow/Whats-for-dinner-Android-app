package com.example.possiblythelastnewproject.backup.ui.viewModel

import kotlinx.serialization.Serializable
import com.example.possiblythelastnewproject.features.pantry.data.entities.*
import com.example.possiblythelastnewproject.features.recipe.data.entities.*
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.*

@Serializable
data class FullDatabaseBackup(
    val version: Int = 1, // default to version 1
    val pantryItems: List<PantryItem>,
    val recipes: List<Recipe>,
    val recipePantryRefs: List<RecipePantryItemCrossRef>,
    val shoppingLists: List<ShoppingList>,
    val shoppingListItems: List<ShoppingListItem>,
    val recipeSelections: List<RecipeSelection>,
    val undoActions: List<UndoAction>,
    val categories: List<Category>
)