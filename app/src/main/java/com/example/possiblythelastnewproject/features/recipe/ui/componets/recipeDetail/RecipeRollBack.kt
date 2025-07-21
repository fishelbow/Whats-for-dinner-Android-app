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
    viewModel: RecipesViewModel
): () -> Unit = {
    CoroutineScope(Dispatchers.Main).launch {
        val snapshot = viewModel.getRecipeWithIngredients(recipeId)
        val crossRefs = viewModel.ingredientRepository.getCrossRefsForRecipeOnce(recipeId)

        snapshot?.let {
            val pending = viewModel.uiState.value.pendingImageUri
            val committed = viewModel.uiState.value.imageUri

            if (!pending.isNullOrBlank() && pending != committed) {
                deleteImageFromStorage(pending, context)
                viewModel.rollbackImageUri()
            }

            val restoredState = RecipeEditUiState.snapshotFrom(it, crossRefs)
            viewModel.updateUi { restoredState }
            viewModel.restoreRecipeState(restoredState.toRecipeModel(it.recipe), restoredState.ingredients)
        }
    }
}