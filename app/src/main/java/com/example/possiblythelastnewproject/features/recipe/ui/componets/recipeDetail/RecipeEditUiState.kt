package com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeDetail

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.TextFieldValue
import com.example.possiblythelastnewproject.features.recipe.data.RecipeWithIngredients
import com.example.possiblythelastnewproject.features.recipe.data.entities.Recipe
import com.example.possiblythelastnewproject.features.recipe.data.entities.RecipePantryItemCrossRef
import com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeCreation.RecipeIngredientUI

data class RecipeEditUiState(
    val name: TextFieldValue = TextFieldValue(""),
    val temp: TextFieldValue = TextFieldValue(""),
    val prepTime: TextFieldValue = TextFieldValue(""),
    val cookTime: TextFieldValue = TextFieldValue(""),
    val category: TextFieldValue = TextFieldValue(""),
    val instructions: TextFieldValue = TextFieldValue(""),
    val cardColor: Color = Color.White,
    val imageUri: String? = null,
    val pendingImageUris: List<String> = emptyList(),
    val currentImageIndex: Int = -1,
    val ingredients: List<RecipeIngredientUI> = emptyList(),
    val newIngredient: String = "",
    val lastStableImageUri: String? = imageUri,
    val originalImageUri: String? = null,
    val preservedImageUri: String? = null,
    val isEditing: Boolean = false,


    ) {

    val currentDisplayUri: String?
        get() = pendingImageUris.getOrNull(currentImageIndex) ?: imageUri

    val hasPendingImageChange: Boolean
        get() = pendingImageUris.isNotEmpty() && currentDisplayUri != imageUri

    fun commitImage(): RecipeEditUiState =
        copy(imageUri = currentDisplayUri, pendingImageUris = emptyList(), currentImageIndex = -1)

    fun rollbackImages(): RecipeEditUiState =
        copy(
            imageUri = lastStableImageUri,
            pendingImageUris = emptyList(),
            currentImageIndex = -1
        )

    fun toRecipeModel(original: Recipe): Recipe =
        original.copy(
            name = name.text.trim(),
            temp = temp.text.trim(),
            prepTime = prepTime.text.trim(),
            cookTime = cookTime.text.trim(),
            category = category.text.trim(),
            instructions = instructions.text.trim(),
            imageUri = imageUri.orEmpty(),
            color = cardColor.toArgb()
        )

    fun hasAnyChangesComparedTo(
        original: RecipeWithIngredients,
        crossRefs: List<RecipePantryItemCrossRef>
    ): Boolean {
        val snapshot = snapshotFrom(original, crossRefs, overrideImage = imageUri)
        return this != snapshot || hasPendingImageChange
    }

    companion object {
        fun snapshotFrom(
            original: RecipeWithIngredients,
            crossRefs: List<RecipePantryItemCrossRef>,
            overrideImage: String? = null,
        ): RecipeEditUiState {
            val refMap = crossRefs.associateBy { it.pantryItemId }
            val enrichedIngredients = original.ingredients.mapNotNull { pantry ->
                refMap[pantry.id]?.let { ref ->
                    RecipeIngredientUI(
                        name = pantry.name,
                        pantryItemId = pantry.id,
                        includeInShoppingList = pantry.addToShoppingList,
                        includeInPantry = true,
                        hasScanCode = pantry.scanCode?.isNotBlank() == true,
                        amountNeeded = ref.amountNeeded,
                        required = ref.required
                    )
                }
            }

            return RecipeEditUiState(
                name = TextFieldValue(original.recipe.name),
                temp = TextFieldValue(original.recipe.temp),
                prepTime = TextFieldValue(original.recipe.prepTime),
                cookTime = TextFieldValue(original.recipe.cookTime),
                category = TextFieldValue(original.recipe.category),
                instructions = TextFieldValue(original.recipe.instructions),
                cardColor = Color(original.recipe.color),
                imageUri = overrideImage ?: original.recipe.imageUri,
                pendingImageUris = emptyList(),
                currentImageIndex = -1,
                ingredients = enrichedIngredients,
                newIngredient = ""
            )
        }
    }
}