package com.example.possiblythelastnewproject.debug

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.possiblythelastnewproject.features.pantry.data.PantryRepository
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipePantryItemRepository
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebugViewModel @Inject constructor(
    private val pantryRepo: PantryRepository,
    private val recipeRepo: RecipeRepository,
    private val crossRefRepo: RecipePantryItemRepository
) : ViewModel() {

    val isLoading = mutableStateOf(false)
    val progress = mutableStateOf(0f)

    fun loadTestData(context: Context, pantryCount: Int, recipeCount: Int, imageUri: Uri?) {
        viewModelScope.launch {
            isLoading.value = true
            progress.value = 0f

            val mockImageUri = saveMockImageToInternalStorage(context)

            populateTestDataWithImage(
                imageUri = mockImageUri,
                context = context,
                pantryRepo = pantryRepo,
                recipeRepo = recipeRepo,
                crossRefRepo = crossRefRepo,
                pantryCount = pantryCount,
                recipeCount = recipeCount,
                onProgress = { progress.value = it }
            )

            isLoading.value = false
            progress.value = 1f
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            isLoading.value = true
            progress.value = 0f

            // Clear all data from the repositories
            pantryRepo.clearAll()
            recipeRepo.clearAll()
            crossRefRepo.clearAll()

            progress.value = 1f
            isLoading.value = false
        }
    }
}