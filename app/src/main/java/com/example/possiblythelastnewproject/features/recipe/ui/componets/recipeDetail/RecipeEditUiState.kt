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
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RecipeEditUiState

        if (name != other.name) return false
        if (temp != other.temp) return false
        if (prepTime != other.prepTime) return false
        if (cookTime != other.cookTime) return false
        if (category != other.category) return false
        if (instructions != other.instructions) return false
        if (cardColor != other.cardColor) return false
        if (imageData != null) {
            if (other.imageData == null) return false
            if (!imageData.contentEquals(other.imageData)) return false
        } else if (other.imageData != null) return false
        if (ingredients != other.ingredients) return false
        if (newIngredient != other.newIngredient) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + temp.hashCode()
        result = 31 * result + prepTime.hashCode()
        result = 31 * result + cookTime.hashCode()
        result = 31 * result + category.hashCode()
        result = 31 * result + instructions.hashCode()
        result = 31 * result + cardColor.hashCode()
        result = 31 * result + (imageData?.contentHashCode() ?: 0)
        result = 31 * result + ingredients.hashCode()
        result = 31 * result + newIngredient.hashCode()
        return result
    }
}