package com.example.possiblythelastnewproject.features.recipe.ui

import RecipeEditUiState
import android.content.Context
import androidx.compose.ui.graphics.Color
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
import com.example.possiblythelastnewproject.core.utils.deleteImageFromStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.TextFieldValue
import com.example.possiblythelastnewproject.features.recipe.ui.componets.EditingGuard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class RecipesViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    val ingredientRepository: RecipePantryItemRepository,
    private val recipeDao: RecipeDao
) : ViewModel() {

    val _uiState = MutableStateFlow(RecipeEditUiState())
    val uiState: StateFlow<RecipeEditUiState> = _uiState

    private var originalIngredients: List<RecipeIngredientUI> = emptyList()


    fun updateName(value: TextFieldValue) = updateUi { copy(name = value) }
    fun updateTemp(value: TextFieldValue) = updateUi { copy(temp = value) }
    fun updatePrepTime(value: TextFieldValue) = updateUi { copy(prepTime = value) }
    fun updateCookTime(value: TextFieldValue) = updateUi { copy(cookTime = value) }
    fun updateCategory(value: TextFieldValue) = updateUi { copy(category = value) }
    fun updateInstructions(value: TextFieldValue) = updateUi { copy(instructions = value) }
    fun updateCardColor(color: Color) = updateUi { copy(cardColor = color) }
    fun updateImageUri(uri: String) = updateUi { copy(imageUri = uri) }
    fun updateIngredients(list: List<RecipeIngredientUI>) = updateUi { copy(ingredients = list) }
    fun updateNewIngredient(value: String) = updateUi { copy(newIngredient = value) }

    inline fun updateUi(transform: RecipeEditUiState.() -> RecipeEditUiState) {
        _uiState.value = _uiState.value.transform()
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
        crossRefs.forEach { ingredientRepository.insertCrossRef(it) }
    }

    fun updateRecipeWithIngredientsUi(
        updatedRecipe: Recipe,
        updatedIngredients: List<RecipeIngredientUI>,
        originalImageUri: String?,
        context: Context
    ) = viewModelScope.launch {
        if (updatedRecipe.imageUri != originalImageUri) {
            val wasDeleted = originalImageUri?.let { deleteImageFromStorage(it, context) } ?: false
            if (wasDeleted && _uiState.value.imageUri == originalImageUri) {
                updateImageUri("")
            }
        }

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

    fun delete(recipe: Recipe, context: Context) = viewModelScope.launch {
        recipeRepository.delete(recipe, context)
        ingredientRepository.deleteCrossRefsForRecipe(recipe.id)

        if (_uiState.value.imageUri == recipe.imageUri) {
            updateImageUri("")
        }
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

    fun restoreRecipeState(
        restoredRecipe: Recipe,
        restoredIngredients: List<RecipeIngredientUI>
    ) = viewModelScope.launch {
        recipeRepository.insert(restoredRecipe)

        val restoredRefs = restoredIngredients
            .filter { it.pantryItemId != null }
            .map {
                RecipePantryItemCrossRef(
                    recipeId = restoredRecipe.id,
                    pantryItemId = it.pantryItemId!!,
                    required = it.required,
                    amountNeeded = it.amountNeeded
                )
            }

        ingredientRepository.replaceIngredientsForRecipe(restoredRecipe.id, restoredRefs)
    }

    fun discardAndExitIfEditing(
        recipeId: Long,
        context: Context,
        editingGuard: EditingGuard,
        uiState: RecipeEditUiState,
        originalImageUri: String,
        updateImagePath: (String) -> Unit,
        resetUnsavedFlag: () -> Unit,
        exit: () -> Unit
    ) {
        if (editingGuard.isEditing) {
            CoroutineScope(Dispatchers.Main).launch {
                editingGuard.requestExit(
                    rollback = {
                        CoroutineScope(Dispatchers.Main).launch {
                            val latest = getRecipeWithIngredients(recipeId)
                            val freshRefs = ingredientRepository.getCrossRefsForRecipeOnce(recipeId)

                            latest?.let {
                                uiState.rollbackImageIfNeeded(originalImageUri, context, ::deleteImageFromStorage)
                                uiState.applyRecipe(it, freshRefs)

                                val restoredPath = it.recipe.imageUri.orEmpty()
                                updateImagePath(restoredPath)
                                resetUnsavedFlag()

                                val restoredRecipe = uiState.toRecipeModel(it.recipe)
                                restoreRecipeState(restoredRecipe, uiState.ingredients)
                            }
                        }
                    },
                    thenExit = {
                        editingGuard.isEditing = false
                        exit()
                    }
                )
            }
        } else {
            editingGuard.isEditing = false
            exit()
        }
    }
}