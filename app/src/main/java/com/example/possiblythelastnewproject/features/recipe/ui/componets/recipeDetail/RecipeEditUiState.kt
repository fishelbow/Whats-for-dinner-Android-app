package com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeDetail

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeCreation.RecipeIngredientUI

data class RecipeEditUiState(
    val name: TextFieldValue = TextFieldValue(""),
    var temp: TextFieldValue = TextFieldValue(""),
    var prepTime: TextFieldValue = TextFieldValue(""),
    var cookTime: TextFieldValue = TextFieldValue(""),
    var category: TextFieldValue = TextFieldValue(""),
    val instructions: TextFieldValue = TextFieldValue(""),
    var cardColor: Color = Color.White,
    val imageData: ByteArray? = null,
    val ingredients: List<RecipeIngredientUI> = emptyList(),
    val newIngredient: String = ""
)