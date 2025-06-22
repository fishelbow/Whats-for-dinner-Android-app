package com.example.possiblythelastnewproject.features.recipe.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.possiblythelastnewproject.features.recipe.data.RecipeWithIngredients
import com.example.possiblythelastnewproject.features.recipe.data.entities.Recipe
import com.example.possiblythelastnewproject.features.recipe.data.entities.RecipePantryItemCrossRef
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipePantryItemRepository
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipeRepository
import com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeCreation.RecipeIngredientUI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipesViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: RecipePantryItemRepository
) : ViewModel() {


    val recipes: StateFlow<List<RecipeWithIngredients>> =
        recipeRepository.getRecipesWithIngredients()
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

    suspend fun getRecipeById(recipeId: Long): Recipe? {
        return recipeRepository.getRecipeById(recipeId)
    }

    fun save(updated: Recipe) = viewModelScope.launch {
        recipeRepository.insert(updated)
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
}