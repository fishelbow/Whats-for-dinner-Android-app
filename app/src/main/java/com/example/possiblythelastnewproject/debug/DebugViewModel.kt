package com.example.possiblythelastnewproject.debug

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.possiblythelastnewproject.features.pantry.data.PantryRepository
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipePantryItemRepository
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val progress = mutableFloatStateOf(0f)
    val loadingStage = mutableStateOf("Idle")
    val loadingDetail = mutableStateOf("")

    val pantryCount = mutableFloatStateOf(0f)
    val recipeCount = mutableFloatStateOf(0f)
    val ingredientAmount = mutableFloatStateOf(0f)



    fun loadTestData(
        context: Context,
        pantryCount: Int,
        recipeCount: Int,
        ingredientCount: Int,
        generateImage: suspend (String) -> Uri,
        keepScreenAwake: () -> Unit = {},
        releaseScreenAwake: () -> Unit = {}
    ) {
        viewModelScope.launch {
            beginLoading()
            loadingStage.value = "Generating mock images..."

            withContext(Dispatchers.IO) {
                populateTestDataWithImage(
                    context = context,
                    pantryRepo = pantryRepo,
                    recipeRepo = recipeRepo,
                    crossRefRepo = crossRefRepo,
                    pantryCount = pantryCount,
                    recipeCount = recipeCount,
                    ingredientCount = ingredientCount,
                    onInit = { loadingStage.value = "Inserting mock data..." },
                    onProgress = { progress.floatValue = it },
                    onDetail = { loadingDetail.value = it },
                    generateImage = generateImage // ✅ externally provided
                )
            }

            finishLoading()
        }
    }

    fun wipeDatabase(context: Context) = viewModelScope.launch {
        beginLoading("Wiping database...")

        withContext(Dispatchers.IO) {
            // Stage 1: Pre-check
            updateProgress(0.05f)
            loadingStage.value = "Scanning internal files"
            val internalFiles = listFilesInAppStorage(context)
            Log.d("ImageCleanup", "Files in internal storage: $internalFiles")

            // Stage 2: DB cleanup
            updateProgress(0.30f)
            loadingStage.value = "Clearing DB entries"
            loadingDetail.value = "Calling debugRepo.clearDbEntries()"
            debugRepo.clearDbEntries()

            // Stage 3: Image deletion
            updateProgress(0.70f)
            loadingStage.value = "Deleting Images"
            loadingDetail.value = "Calling debugRepo.deleteAllAppImages(context)"
            debugRepo.deleteAllAppImages(context)

            val remainingImages = File(context.filesDir, "images").listFiles()?.size ?: 0
            Log.d("ImageCleanup", "Images remaining after wipe: $remainingImages")

            // Final cleanup
            updateProgress(1.0f)
            loadingStage.value = "Final cleanup"
            loadingDetail.value = "Reset complete"
        }

        endLoading()
    }

    private fun listFilesInAppStorage(context: Context): List<String> {
        return context.filesDir.listFiles()
            ?.filter { it.isFile }
            ?.map { it.name }
            ?: emptyList()
    }

    fun beginLoading(initialStage: String = "Starting...") {
        isLoading.value = true
        progress.floatValue = 0f
        loadingStage.value = initialStage
        loadingDetail.value = ""
    }

    private fun finishLoading() {
        progress.floatValue = 1f
        isLoading.value = false
        loadingStage.value = "Idle"
    }


    private fun updateProgress(percent: Float) {
        viewModelScope.launch {
            progress.floatValue = percent.coerceIn(0f, 1f)
        }
    }

    private fun endLoading() {
        isLoading.value = false
        loadingStage.value = ""
        loadingDetail.value = ""
    }
}