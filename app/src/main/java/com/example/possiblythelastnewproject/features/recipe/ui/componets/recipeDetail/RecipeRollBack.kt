package com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeDetail

import RecipeEditUiState
import android.content.Context
import com.example.possiblythelastnewproject.core.utils.deleteImageFromStorage
import com.example.possiblythelastnewproject.features.recipe.ui.RecipesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun performRecipeRollback(
    recipeId: Long,
    context: Context,
    viewModel: RecipesViewModel,
    uiState: RecipeEditUiState,
    originalImageUri: String,
    setImagePath: (String) -> Unit,
    resetUnsavedFlag: () -> Unit
): () -> Unit = {
    CoroutineScope(Dispatchers.Main).launch {
        val latest = viewModel.getRecipeWithIngredients(recipeId)
        val freshRefs = viewModel.ingredientRepository.getCrossRefsForRecipeOnce(recipeId)

        latest?.let {
            uiState.rollbackImageIfNeeded(originalImageUri, context, ::deleteImageFromStorage)
            uiState.applyRecipe(it, freshRefs)

            val image = it.recipe.imageUri.orEmpty()
            setImagePath(image)
            resetUnsavedFlag()

            val restored = uiState.toRecipeModel(it.recipe)
            viewModel.restoreRecipeState(restored, uiState.ingredients)
        }
    }
}