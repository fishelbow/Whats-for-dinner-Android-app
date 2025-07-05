package com.example.possiblythelastnewproject.features.recipe.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.recipe.data.RecipeWithIngredients
import com.example.possiblythelastnewproject.features.recipe.data.dao.RecipeDao
import com.example.possiblythelastnewproject.features.recipe.data.entities.Recipe
import com.example.possiblythelastnewproject.features.recipe.data.entities.RecipePantryItemCrossRef
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipePantryItemRepository
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipeRepository
import com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeCreation.RecipeIngredientUI
import com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeDetail.RecipeEditUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipesViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: RecipePantryItemRepository,
    private val recipeDao: RecipeDao

) : ViewModel() {
    var editUiState by mutableStateOf(RecipeEditUiState())
        private set

    // Load full recipe into UI state
    fun loadRecipeIntoUiState(recipe: RecipeWithIngredients) {
        editUiState = RecipeEditUiState(
            name = TextFieldValue(recipe.recipe.name),
            temp = TextFieldValue(recipe.recipe.temp),
            prepTime = TextFieldValue(recipe.recipe.prepTime),
            cookTime = TextFieldValue(recipe.recipe.cookTime),
            category = TextFieldValue(recipe.recipe.category),
            instructions = TextFieldValue(recipe.recipe.instructions),
            cardColor = Color(recipe.recipe.color),
            imageData = recipe.recipe.imageData,
            ingredients = recipe.ingredients.map {
                RecipeIngredientUI(
                    name = it.name,
                    pantryItemId = it.id,
                    isShoppable = false,
                    hasScanCode = false
                )
            },
            newIngredient = ""
        )
    }

    // Update individual fields
    fun updateName(value: TextFieldValue) {
        editUiState = editUiState.copy(name = value)
    }

    fun updateTemp(value: TextFieldValue) {
        editUiState = editUiState.copy(temp = value)
    }

    fun updatePrepTime(value: TextFieldValue) {
        editUiState = editUiState.copy(prepTime = value)
    }

    fun updateCookTime(value: TextFieldValue) {
        editUiState = editUiState.copy(cookTime = value)
    }

    fun updateCategory(value: TextFieldValue) {
        editUiState = editUiState.copy(category = value)
    }

    fun updateInstructions(value: TextFieldValue) {
        editUiState = editUiState.copy(instructions = value)
    }

    fun updateCardColor(color: Color) {
        editUiState = editUiState.copy(cardColor = color)
    }

    fun updateImageData(data: ByteArray?) {
        editUiState = editUiState.copy(imageData = data)
    }

    fun updateIngredients(list: List<RecipeIngredientUI>) {
        editUiState = editUiState.copy(ingredients = list)
    }

    fun updateNewIngredient(value: String) {
        editUiState = editUiState.copy(newIngredient = value)
    }



    // Optional: helper to reset state
    fun clearUiState() {
        editUiState = RecipeEditUiState()
    }

    // Optional: compare current state to original recipe
    fun hasUnsavedChanges(original: RecipeWithIngredients): Boolean {
        val s = editUiState
        return s.name.text != original.recipe.name ||
                s.temp.text != original.recipe.temp ||
                s.prepTime.text != original.recipe.prepTime ||
                s.cookTime.text != original.recipe.cookTime ||
                s.category.text != original.recipe.category ||
                s.instructions.text != original.recipe.instructions ||
                s.cardColor.toArgb() != original.recipe.color ||
                s.imageData?.contentEquals(original.recipe.imageData) == false ||
                s.ingredients.map { it.name } != original.ingredients.map { it.name }
    }

// Add similar functions for temp, prepTime, etc.

val allRecipes: StateFlow<List<Recipe>> =
    recipeRepository.getAllRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

val recipes: StateFlow<List<RecipeWithIngredients>> =
    recipeRepository.getRecipesWithIngredients()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    suspend fun recipeNameExists(name: String, excludeUuid: String? = null): Boolean {
        return if (excludeUuid != null) {
            recipeDao.existsByNameExcludingUuid(name.trim(), excludeUuid)
        } else {
            recipeDao.existsByName(name.trim())
        }
    }

        fun saveRecipeWithIngredientsUi(
    recipe: Recipe,
    ingredients: List<RecipeIngredientUI>
) = viewModelScope.launch {
    val recipeId = recipeRepository.insert(recipe.copy(id = 0L))

    val crossRefs = ingredients
        .filter { it.pantryItemId != null }
        .map {
            RecipePantryItemCrossRef(
                recipeId = recipeId,
                pantryItemId = it.pantryItemId!!,
                required = it.required,
                amountNeeded = it.amountNeeded
            )
        }

    crossRefs.forEach { ref ->
        ingredientRepository.insertCrossRef(ref)
    }
}

fun delete(recipe: Recipe) = viewModelScope.launch {
    recipeRepository.delete(recipe)
    ingredientRepository.deleteCrossRefsForRecipe(recipe.id)
}

suspend fun getRecipeWithIngredients(recipeId: Long): RecipeWithIngredients? {
    return recipeRepository.getRecipeWithIngredients(recipeId)
}

fun updateRecipeWithIngredientsUi(
    updatedRecipe: Recipe,
    updatedIngredients: List<RecipeIngredientUI>
) = viewModelScope.launch {
    recipeRepository.insert(updatedRecipe) // insert() should upsert

    val newRefs = updatedIngredients
        .filter { it.pantryItemId != null }
        .map {
            RecipePantryItemCrossRef(
                recipeId = updatedRecipe.id,
                pantryItemId = it.pantryItemId!!,
                required = it.required,
                amountNeeded = it.amountNeeded
            )
        }

    ingredientRepository.replaceIngredientsForRecipe(updatedRecipe.id, newRefs)
}
fun observeIngredientsForRecipe(
    recipeId: Long,
    pantryItems: List<PantryItem>
): Flow<List<RecipeIngredientUI>> =
    ingredientRepository.observeCrossRefsForRecipe(recipeId).map { crossRefs ->
        crossRefs.map { ref ->
            val pantry = pantryItems.firstOrNull { it.id == ref.pantryItemId }
            RecipeIngredientUI(
                name = pantry?.name ?: "Unknown",
                pantryItemId = ref.pantryItemId,
                amountNeeded = ref.amountNeeded,
                required = ref.required,
                hasScanCode = pantry?.scanCode?.isNotBlank() == true
            )
        }
    }


}

