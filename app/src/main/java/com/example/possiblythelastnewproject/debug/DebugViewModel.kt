package com.example.possiblythelastnewproject.debug

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.possiblythelastnewproject.features.pantry.data.PantryRepository
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipePantryItemRepository
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DebugViewModel @Inject constructor(
    private val pantryRepo: PantryRepository,
    private val recipeRepo: RecipeRepository,
    private val crossRefRepo: RecipePantryItemRepository,
    private val debugRepo: DebugRepository
) : ViewModel() {

    val isLoading = mutableStateOf(false)
    val progress = mutableStateOf(0f)

    fun loadTestData(context: Context, pantryCount: Int, recipeCount: Int, ingredientCount: Int, imageUri: Uri?) {
        viewModelScope.launch {
            isLoading.value = true
            progress.value = 0f

            // 🔍 Validate imageUri or fallback to a safe mock image
            val validatedImageUri = try {
                context.contentResolver.openInputStream(imageUri ?: Uri.EMPTY)?.close()
                imageUri ?: saveMockImageToInternalStorage(context)
            } catch (e: Exception) {
                saveMockImageToInternalStorage(context)
            }

            // 🚀 Generate debug data using validated image
            populateTestDataWithImage(
                imageUri = validatedImageUri,
                context = context,
                pantryRepo = pantryRepo,
                recipeRepo = recipeRepo,
                crossRefRepo = crossRefRepo,
                pantryCount = pantryCount,
                recipeCount = recipeCount,
                ingredientCount = ingredientCount,
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

            // 🧹 Clear all database tables for fresh testing
            debugRepo.clearDbEntries()
            progress.value = 1f
            isLoading.value = false
        }
    }

    fun wipeDatabase(context: Context) {
        viewModelScope.launch {
            isLoading.value = true
            progress.value = 0f

            // delete db of entries
            debugRepo.clearDbEntries()
            debugRepo.deleteAllAppImages(context)

            // remove created images
            val imageDir = File(context.filesDir, "images")
            val remaining = imageDir.listFiles()?.size ?: 0
            Log.d("ImageCleanup", "Images remaining after wipe: $remaining")

            progress.value = 1f
            isLoading.value = false
        }
    }
}