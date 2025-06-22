package com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeCreation

data class RecipeIngredientUI(
    val name: String,
    val pantryItemId: Long? = null,
    val hasScanCode: Boolean = false,
    val isShoppable: Boolean = false,
    val required: Boolean = true,
    val amountNeeded: String = "",
    val includeInShoppingList: Boolean = true,
    val includeInPantry: Boolean = true
)