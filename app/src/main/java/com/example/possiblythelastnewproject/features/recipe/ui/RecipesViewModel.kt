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
import androidx.core.net.toUri

@HiltViewModel
class RecipesViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: RecipePantryItemRepository,
    private val recipeDao: RecipeDao
) : ViewModel() {

    var editUiState by mutableStateOf(RecipeEditUiState())
        private set

    private var originalIngredients: List<RecipeIngredientUI> = emptyList()

    // Load full recipe into UI state
    suspend fun loadRecipeIntoUiState(
        recipe: RecipeWithIngredients,
        pantryItems: List<PantryItem>
    ) {
        val pantryMap = pantryItems.associateBy { it.id }
        val crossRefs = ingredientRepository.getCrossRefsForRecipeOnce(recipe.recipe.id)
        val crossRefMap = crossRefs.associateBy { it.pantryItemId }

        val ingredients = recipe.ingredients.mapNotNull { pantry ->
            val ref = crossRefMap[pantry.id]
            ref?.let {
                RecipeIngredientUI(
                    name = pantry.name,
                    pantryItemId = pantry.id,
                    includeInShoppingList = pantry.addToShoppingList,
                    includeInPantry = true,
                    hasScanCode = pantry.scanCode?.isNotBlank() == true,
                    amountNeeded = it.amountNeeded,
                    required = it.required
                )
            }
        }

        originalIngredients = ingredients

        editUiState = RecipeEditUiState(
            name = TextFieldValue(recipe.recipe.name),
            temp = TextFieldValue(recipe.recipe.temp),
            prepTime = TextFieldValue(recipe.recipe.prepTime),
            cookTime = TextFieldValue(recipe.recipe.cookTime),
            category = TextFieldValue(recipe.recipe.category),
            instructions = TextFieldValue(recipe.recipe.instructions),
            cardColor = Color(recipe.recipe.color),
            imageUri = recipe.recipe.imageUri?.toUri().toString(),
            ingredients = ingredients,
            newIngredient = ""
        )
    }

    // Update individual fields
    fun updateName(value: TextFieldValue) = updateUi { copy(name = value) }
    fun updateTemp(value: TextFieldValue) = updateUi { copy(temp = value) }
    fun updatePrepTime(value: TextFieldValue) = updateUi { copy(prepTime = value) }
    fun updateCookTime(value: TextFieldValue) = updateUi { copy(cookTime = value) }
    fun updateCategory(value: TextFieldValue) = updateUi { copy(category = value) }
    fun updateInstructions(value: TextFieldValue) = updateUi { copy(instructions = value) }
    fun updateCardColor(color: Color) = updateUi { copy(cardColor = color) }
    fun updateImageUri(uri: String) = updateUi { copy(imageUri = uri.toString()) }
    fun updateIngredients(list: List<RecipeIngredientUI>) = updateUi { copy(ingredients = list) }
    fun updateNewIngredient(value: String) = updateUi { copy(newIngredient = value) }

    private inline fun updateUi(update: RecipeEditUiState.() -> RecipeEditUiState) {
        editUiState = editUiState.update()
    }

    fun hasUnsavedChanges(original: RecipeWithIngredients): Boolean {
        val current = editUiState
        return hasRecipeFieldChanges(current, original.recipe) ||
                hasImageUriChanged(current.imageUri, original.recipe.imageUri) ||
                hasIngredientChanges(current.ingredients)
    }

    private fun hasRecipeFieldChanges(current: RecipeEditUiState, original: Recipe): Boolean {
        return current.name.text != original.name ||
                current.temp.text != original.temp ||
                current.prepTime.text != original.prepTime ||
                current.cookTime.text != original.cookTime ||
                current.category.text != original.category ||
                current.instructions.text != original.instructions ||
                current.cardColor.toArgb() != original.color
    }

    private fun hasImageChanged(currentImage: ByteArray?, originalImage: ByteArray?): Boolean {
        return currentImage?.contentEquals(originalImage) == false
    }

    private fun hasIngredientChanges(current: List<RecipeIngredientUI>): Boolean {
        val currentMap = current.associateBy { it.pantryItemId }
        val originalMap = originalIngredients.associateBy { it.pantryItemId }

        // Check for added or changed ingredients
        val addedOrChanged = currentMap.any { (id, curr) ->
            val orig = originalMap[id]
            orig == null || curr.includeInShoppingList != orig.includeInShoppingList ||
                    curr.amountNeeded != orig.amountNeeded ||
                    curr.required != orig.required
        }

        //  Check for removed ingredients
        val removed = originalMap.keys.any { it !in currentMap }

        return addedOrChanged || removed
    }


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

    fun updateRecipeWithIngredientsUi(
        updatedRecipe: Recipe,
        updatedIngredients: List<RecipeIngredientUI>
    ) = viewModelScope.launch {
        recipeRepository.insert(updatedRecipe)

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

    fun delete(recipe: Recipe) = viewModelScope.launch {
        recipeRepository.delete(recipe)
        ingredientRepository.deleteCrossRefsForRecipe(recipe.id)
    }

    suspend fun getRecipeWithIngredients(recipeId: Long): RecipeWithIngredients? {
        return recipeRepository.getRecipeWithIngredients(recipeId)
    }

    fun observeIngredientsForRecipe(
        recipeId: Long,
        pantryItems: List<PantryItem>
    ): Flow<List<RecipeIngredientUI>> =
        ingredientRepository.observeCrossRefsForRecipe(recipeId).map { crossRefs ->
            val pantryMap = pantryItems.associateBy { it.id }
            crossRefs.map { ref ->
                val pantry = pantryMap[ref.pantryItemId]
                RecipeIngredientUI(
                    name = pantry?.name ?: "Unknown",
                    pantryItemId = ref.pantryItemId,
                    amountNeeded = ref.amountNeeded,
                    required = ref.required,
                    hasScanCode = pantry?.scanCode?.isNotBlank() == true,
                    includeInShoppingList = pantry?.addToShoppingList == true
                )
            }
        }
    private fun hasImageUriChanged(currentUri: String?, originalUri: String?): Boolean {
        return currentUri?.toString() != originalUri
    }
}