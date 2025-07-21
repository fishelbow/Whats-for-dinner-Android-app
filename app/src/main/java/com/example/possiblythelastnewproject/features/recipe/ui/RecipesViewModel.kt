package com.example.possiblythelastnewproject.features.recipe.ui

import RecipeEditUiState
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import javax.inject.Inject

@HiltViewModel
class RecipesViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    val ingredientRepository: RecipePantryItemRepository,
    private val recipeDao: RecipeDao
) : ViewModel() {

    // 🧠 UI Edit State
    val _uiState = MutableStateFlow(RecipeEditUiState())
    val uiState: StateFlow<RecipeEditUiState> = _uiState.asStateFlow()

    inline fun updateUi(transform: RecipeEditUiState.() -> RecipeEditUiState) {
        _uiState.update { it.transform() }
    }

    // 🖍 Field Updaters
    fun updateCardColor(color: Color) = updateUi { copy(cardColor = color) }
    fun updateIngredients(list: List<RecipeIngredientUI>) = updateUi { copy(ingredients = list) }
    fun updatePendingImageUri(uri: String) = updateUi { withPendingImage(uri) }
    fun commitImageUri() = updateUi { commitImage() }
    fun rollbackImageUri() = updateUi { rollbackImage() }

    var activeRecipeId by mutableStateOf<Long?>(null)

    // 📦 Recipe Flows
    val allRecipes: StateFlow<List<Recipe>> =
        recipeRepository.getAllRecipes()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recipes: StateFlow<List<RecipeWithIngredients>> =
        recipeRepository.getRecipesWithIngredients()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 🧭 Snapshot Loader
    fun loadRecipe(recipeId: Long) = viewModelScope.launch {
        val snapshot = recipeRepository.getRecipeWithIngredients(recipeId) ?: return@launch
        val crossRefs = ingredientRepository.getCrossRefsForRecipeOnce(recipeId)
        _uiState.value = RecipeEditUiState.snapshotFrom(snapshot, crossRefs)
    }

    // 🕵️ Name Collision
    suspend fun recipeNameExists(name: String, excludeUuid: String? = null): Boolean {
        val trimmed = name.trim()
        return if (excludeUuid != null)
            recipeDao.existsByNameExcludingUuid(trimmed, excludeUuid)
        else
            recipeDao.existsByName(trimmed)
    }

    fun discardImageIfNeeded(context: Context): Boolean {
        val pending = _uiState.value.pendingImageUri
        val confirmed = _uiState.value.imageUri

        if (!pending.isNullOrBlank() && pending != confirmed) {
            val result = deleteImageFromStorage(pending, context)
            updateUi { rollbackImage() }
            return result
        }
        return false
    }

    fun guardedExit(context: Context, navAction: () -> Unit) {
        discardImageIfNeeded(context)
        updateUi { rollbackImage() }
        navAction()
    }

    // 💾 Save New Recipe
    fun saveRecipeWithIngredientsUi(
        recipe: Recipe,
        ingredients: List<RecipeIngredientUI>,
        context: Context
    ) = viewModelScope.launch {
        if (_uiState.value.hasPendingImageChange) {
            commitImageUri()
        }

        val recipeId = recipeRepository.insert(recipe.copy(id = 0L))
        val crossRefs = ingredients.filter { it.pantryItemId != null }.map {
            RecipePantryItemCrossRef(
                recipeId     = recipeId,
                pantryItemId = it.pantryItemId!!,
                required     = it.required,
                amountNeeded = it.amountNeeded
            )
        }

        crossRefs.forEach { ingredientRepository.insertCrossRef(it) }
        // 🧼 Room should auto-emit; no manual refresh required
    }

    // 💾 Update Existing Recipe
    fun updateRecipeWithIngredientsUi(
        updatedRecipe: Recipe,
        updatedIngredients: List<RecipeIngredientUI>,
        context: Context
    ) = viewModelScope.launch {
        val state = _uiState.value

        if (state.hasPendingImageChange) {
            state.pendingImageUri?.let { deleteImageFromStorage(it, context) }
            commitImageUri()
        }

        recipeRepository.insert(updatedRecipe)

        val refs = updatedIngredients.filter { it.pantryItemId != null }.map {
            RecipePantryItemCrossRef(
                recipeId     = updatedRecipe.id,
                pantryItemId = it.pantryItemId!!,
                required     = it.required,
                amountNeeded = it.amountNeeded
            )
        }

        ingredientRepository.replaceIngredientsForRecipe(updatedRecipe.id, refs)
    }

    // 🗑 Delete Recipe
    fun deleteRecipe(recipe: Recipe, context: Context) = viewModelScope.launch {
        recipeRepository.delete(recipe, context)
        ingredientRepository.deleteCrossRefsForRecipe(recipe.id)

        updateUi {
            if (imageUri == recipe.imageUri) copy(imageUri = null, pendingImageUri = null)
            else this
        }
    }

    // 🔄 Restore From Snapshot
    fun restoreRecipeState(
        restoredRecipe: Recipe,
        restoredIngredients: List<RecipeIngredientUI>
    ) = viewModelScope.launch {
        recipeRepository.insert(restoredRecipe)

        val refs = restoredIngredients.filter { it.pantryItemId != null }.map {
            RecipePantryItemCrossRef(
                recipeId     = restoredRecipe.id,
                pantryItemId = it.pantryItemId!!,
                required     = it.required,
                amountNeeded = it.amountNeeded
            )
        }

        ingredientRepository.replaceIngredientsForRecipe(restoredRecipe.id, refs)
    }

    // 🎯 Snapshot Getter
    suspend fun getRecipeWithIngredients(recipeId: Long): RecipeWithIngredients? {
        return recipeRepository.getRecipeWithIngredients(recipeId)
    }


}