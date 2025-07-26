package com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeDetail

import android.content.Context
import com.example.possiblythelastnewproject.core.utils.deleteImageFromStorage
import com.example.possiblythelastnewproject.features.recipe.ui.RecipesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.possiblythelastnewproject.features.recipe.data.RecipeWithIngredients
import com.example.possiblythelastnewproject.features.recipe.data.entities.RecipePantryItemCrossRef

fun performRecipeRollback(
    recipeId: Long,
    context: Context,
    viewModel: RecipesViewModel
): () -> Unit = {
    CoroutineScope(Dispatchers.Main).launch {
        val snapshot: RecipeWithIngredients? = viewModel.getRecipeWithIngredients(recipeId)
        val crossRefs: List<RecipePantryItemCrossRef> =
            viewModel.ingredientRepository.getCrossRefsForRecipeOnce(recipeId)

        snapshot?.let { it ->
            val state = viewModel.uiState.value
            // Delete all pending URIs except committed one
            state.pendingImageUris
                .filter { it != state.imageUri }
                .forEach { deleteImageFromStorage(it, context) }

            viewModel.rollbackImageUri()

            val restoredState = RecipeEditUiState.snapshotFrom(it, crossRefs)
            viewModel.updateUi { restoredState }
            viewModel.restoreRecipeState(
                restoredState.toRecipeModel(it.recipe),
                restoredState.ingredients
            )
        }
    }
}