package com.example.possiblythelastnewproject.debug

import android.util.Log
import com.example.possiblythelastnewproject.features.pantry.data.PantryRepository
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.recipe.data.entities.Recipe
import com.example.possiblythelastnewproject.features.recipe.data.entities.RecipePantryItemCrossRef
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipePantryItemRepository
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipeRepository
import kotlinx.coroutines.flow.first

suspend fun populateTestDataWithImage(
    imageBytes: ByteArray,
    pantryRepo: PantryRepository,
    recipeRepo: RecipeRepository,
    crossRefRepo: RecipePantryItemRepository,
    pantryCount: Int,
    recipeCount: Int,
    onProgress: (Float) -> Unit = {}
) {
    val categories = listOf("Grains", "Vegetables", "Fruits", "Dairy", "Proteins", "Snacks", "Spices")

    val totalSteps = pantryCount + recipeCount + recipeCount // pantry + recipes + cross-refs
    var completedSteps = 0
    fun reportProgress() = onProgress(completedSteps.toFloat() / totalSteps)

    // 1. Insert Pantry Items
    val pantryItems = (1..pantryCount).map { i ->
        PantryItem(
            name = "Item $i",
            quantity = (0..10).random(),
            category = categories.random(),
            imageData = imageBytes
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
            imageData = imageBytes
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

    // 3. Insert Cross-References
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

    Log.d("TestData", "Inserted $pantryCount pantry items, $recipeCount recipes, and ${recipeCount * 50} cross-refs with image.")
}