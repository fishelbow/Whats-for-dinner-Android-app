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
    val imageUri: String? = null,
    val ingredients: List<RecipeIngredientUI> = emptyList(),
    val newIngredient: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RecipeEditUiState

        return name == other.name &&
                temp == other.temp &&
                prepTime == other.prepTime &&
                cookTime == other.cookTime &&
                category == other.category &&
                instructions == other.instructions &&
                cardColor == other.cardColor &&
                imageUri == other.imageUri && // ✅ Simple string comparison
                ingredients == other.ingredients &&
                newIngredient == other.newIngredient
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + temp.hashCode()
        result = 31 * result + prepTime.hashCode()
        result = 31 * result + cookTime.hashCode()
        result = 31 * result + category.hashCode()
        result = 31 * result + instructions.hashCode()
        result = 31 * result + cardColor.hashCode()
        result = 31 * result + (imageUri?.hashCode() ?: 0) // ✅ URI-based hash
        result = 31 * result + ingredients.hashCode()
        result = 31 * result + newIngredient.hashCode()
        return result


    }
}