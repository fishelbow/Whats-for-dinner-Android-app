package com.example.possiblythelastnewproject.debug

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.graphics.Color
import androidx.core.net.toUri
import com.example.possiblythelastnewproject.features.pantry.data.PantryRepository
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.recipe.data.entities.Recipe
import com.example.possiblythelastnewproject.features.recipe.data.entities.RecipePantryItemCrossRef
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipePantryItemRepository
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipeRepository
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.createBitmap

suspend fun populateTestDataWithImage(
    context: Context, // NEW: Needed for file access
    pantryRepo: PantryRepository,
    recipeRepo: RecipeRepository,
    crossRefRepo: RecipePantryItemRepository,
    pantryCount: Int,
    recipeCount: Int,
    onProgress: (Float) -> Unit = {},
    imageUri: Uri
) {
    val categories = listOf("Grains", "Vegetables", "Fruits", "Dairy", "Proteins", "Snacks", "Spices")
    val totalSteps = pantryCount + recipeCount + recipeCount
    var completedSteps = 0
    fun reportProgress() = onProgress(completedSteps.toFloat() / totalSteps)

    // 0. Preload a test image URI
    val testImageUri = imageUri.takeIf {
        val filePath = it.path
        filePath != null && File(filePath).exists()
    } ?: saveMockImageToInternalStorage(context)

    // 1. Insert Pantry Items
    val pantryItems = (1..pantryCount).map { i ->
        PantryItem(
            name = "Item $i",
            quantity = (0..10).random(),
            category = categories.random(),
            imageUri = testImageUri.toString()
        )
    }
    pantryItems.chunked(500).forEach { chunk ->
        chunk.forEach {
            pantryRepo.insert(it)
            completedSteps++
            reportProgress()
        }
    }

    val allPantry = pantryRepo.getAllPantryItems().first()
    val pantryIdPool = allPantry.map { it.id }

    // 2. Insert Recipes
    val recipes = (1..recipeCount).map { i ->
        Recipe(
            name = "Recipe $i",
            temp = "350°F",
            prepTime = "15 min",
            cookTime = "30 min",
            category = categories.random(),
            instructions = "Step 1: Do something. Step 2: Eat.",
            imageUri = testImageUri.toString()
        )
    }
    val recipeIds = mutableListOf<Long>()
    recipes.chunked(100).forEach { chunk ->
        chunk.forEach { recipe ->
            val id = recipeRepo.insert(recipe)
            recipeIds.add(id)
            completedSteps++
            reportProgress()
        }
    }

    // 3. Cross-References
    recipeIds.forEach { recipeId ->
        val refs = pantryIdPool.shuffled().take(minOf(50, pantryIdPool.size)).map { pantryId ->
            RecipePantryItemCrossRef(
                recipeId = recipeId,
                pantryItemId = pantryId,
                required = true,
                amountNeeded = "${(1..5).random()}"
            )
        }
        crossRefRepo.replaceIngredientsForRecipe(recipeId, refs)
        completedSteps++
        reportProgress()
    }

    Log.d("TestData", "Inserted $pantryCount pantry items, $recipeCount recipes, and ${recipeCount * 50} cross-refs.")
}

fun saveMockImageToInternalStorage(context: Context): Uri {
    val filename = "test_img.jpg"
    val file = File(context.filesDir, filename)
    if (!file.exists()) {
        val bitmap = createBitmap(200, 200).apply {
            eraseColor(Color.LTGRAY)
        }
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        }
    }
    return file.toUri()
}