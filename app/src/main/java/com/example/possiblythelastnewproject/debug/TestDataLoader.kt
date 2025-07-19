package com.example.possiblythelastnewproject.debug

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.graphics.Color
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import com.example.possiblythelastnewproject.features.pantry.data.PantryRepository
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.recipe.data.entities.Recipe
import com.example.possiblythelastnewproject.features.recipe.data.entities.RecipePantryItemCrossRef
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipePantryItemRepository
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipeRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlin.random.Random
import java.io.File
import java.io.FileOutputStream

suspend fun populateTestDataWithImage(
    context: Context,
    pantryRepo: PantryRepository,
    recipeRepo: RecipeRepository,
    crossRefRepo: RecipePantryItemRepository,
    pantryCount: Int,
    recipeCount: Int,
    ingredientCount: Int,
    onProgress: (Float) -> Unit = {},
    generateImage: suspend (String) -> Uri,
    onInit: () -> Unit,
    onDetail: (String) -> Unit = {}
) {
    onDetail("🚀 Beginning mock data generation...")
    val categories = listOf(
        "Produce", "Snacks", "Dairy", "Meat", "Grains", "Sauce",
        "Frozen", "Drinks", "Paper goods", "Canned goods",
        "Spices", "Toiletries", "Other"
    )
    val recipeColors = listOf(
        0xFFF44336, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7,
        0xFF3F51B5, 0xFF2196F3, 0xFF03A9F4, 0xFF00BCD4,
        0xFF009688, 0xFF4CAF50, 0xFFFFC107, 0xFFFF5722
    ).map { it.toInt() }

    val totalSteps = pantryCount + recipeCount + pantryCount + recipeCount + recipeCount
    var completedSteps = 0
    fun reportProgress() = onProgress(completedSteps.toFloat() / totalSteps)

    onDetail("🧪 Creating pantry items with mock images")
    val pantryItems = (1..pantryCount).map { i ->
        val name = "Item $i"
        onDetail("🔧 Generating pantry image $i of $pantryCount")
        val imageUri = generateImage(name).toString()
        completedSteps++
        reportProgress()

        PantryItem(
            name = name,
            quantity = Random.nextInt(0, 11),
            category = categories.random(),
            imageUri = imageUri
        )
    }

    onDetail("🎨 Creating recipe items with mock images and color tags")
    val recipes = (1..recipeCount).map { i ->
        val name = "Recipe $i"
        onDetail("📸 Generating recipe image $i of $recipeCount")
        val imageUri = generateImage(name).toString()
        completedSteps++
        reportProgress()

        val color = recipeColors.random()
        Recipe(
            name = name,
            temp = "350°F",
            prepTime = "15 min",
            cookTime = "30 min",
            category = categories.random(),
            instructions = "Step 1: Do something. Step 2: Eat.",
            imageUri = imageUri,
            color = color
        )
    }

    onInit()

    onDetail("📦 Beginning pantry inserts")
    pantryItems.chunked(500).forEachIndexed { chunkIndex, chunk ->
        onDetail("📦 Inserting pantry chunk ${chunkIndex + 1} of ${pantryItems.size / 500 + 1}")
        chunk.forEach {
            pantryRepo.insert(it)
            completedSteps++
            reportProgress()
        }
        delay(5)
    }

    onDetail("🍽️ Beginning recipe inserts")
    val pantryIdPool = pantryRepo.getAllPantryItems().first().map { it.id }
    val recipeIds = mutableListOf<Long>()

    recipes.chunked(100).forEachIndexed { chunkIndex, chunk ->
        onDetail("🍽️ Inserting recipe chunk ${chunkIndex + 1} of ${recipes.size / 100 + 1}")
        chunk.forEach { recipe ->
            val id = recipeRepo.insert(recipe)
            recipeIds.add(id)
            completedSteps++
            reportProgress()
        }
        delay(5)
    }

    onDetail("🔗 Linking pantry items to recipes")
    recipeIds.forEachIndexed { i, recipeId ->
        onDetail("🔗 Linking ingredients for Recipe ${i + 1} of ${recipeIds.size}")
        val refs = pantryIdPool.shuffled()
            .take(minOf(ingredientCount, pantryIdPool.size))
            .map { pantryId ->
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
        delay(2)
    }

    onDetail("✅ All steps complete! $pantryCount pantry items, $recipeCount recipes, ${recipeIds.size * ingredientCount} links created.")
    Log.d("TestData", "✅ Finished: $pantryCount pantry items, $recipeCount recipes, ${recipeIds.size * ingredientCount} cross-refs.")
}