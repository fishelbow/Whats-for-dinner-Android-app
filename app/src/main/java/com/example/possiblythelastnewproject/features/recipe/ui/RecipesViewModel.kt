package com.example.possiblythelastnewproject.features.recipe.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.recipe.data.RecipeWithIngredients
import com.example.possiblythelastnewproject.features.recipe.data.entities.Recipe
import com.example.possiblythelastnewproject.features.recipe.data.entities.RecipePantryItemCrossRef
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipePantryItemRepository
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipeRepository
import com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeCreation.RecipeIngredientUI
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
    private val ingredientRepository: RecipePantryItemRepository
) : ViewModel() {


    val allRecipes: StateFlow<List<Recipe>> =
        recipeRepository.getAllRecipes()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recipes: StateFlow<List<RecipeWithIngredients>> =
        recipeRepository.getRecipesWithIngredients()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getIngredientsUi(recipeId: Long, pantryItems: List<PantryItem>): StateFlow<List<RecipeIngredientUI>> =
        ingredientRepository.observeCrossRefsForRecipe(recipeId)
            .map { refs ->
                refs.map { ref ->
                    val pantry = pantryItems.firstOrNull { it.id == ref.pantryItemId }
                    RecipeIngredientUI(
                        name = pantry?.name ?: "Unknown",
                        pantryItemId = ref.pantryItemId,
                        amountNeeded = ref.amountNeeded,
                        required = ref.required,
                        hasScanCode = pantry?.hasScanCode == true
                    )
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun deleteRecipe(id: Long) = viewModelScope.launch {
        // First delete ingredient cross-references to avoid orphan data
        ingredientRepository.deleteCrossRefsForRecipe(id)

        // Then delete the recipe itself
        recipeRepository.deleteRecipeById(id)
    }


}

