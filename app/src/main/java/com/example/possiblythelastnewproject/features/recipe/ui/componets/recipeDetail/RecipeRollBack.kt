package com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeDetail

import RecipeEditUiState
import android.content.Context
import androidx.core.net.toUri
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

            fun uriExists(uri: String?, context: Context): Boolean {
                return try {
                    uri?.toUri()?.let {
                        context.contentResolver.openInputStream(it)?.close()
                        true
                    } ?: false
                } catch (e: Exception) {
                    false
                }
            }

            val image = it.recipe.imageUri.orEmpty()
            val safeImage = if (uriExists(image, context)) image else originalImageUri.orEmpty()

            setImagePath(safeImage)


            resetUnsavedFlag()

            val restored = uiState.toRecipeModel(it.recipe)
            viewModel.restoreRecipeState(restored, uiState.ingredients)
        }
    }
}