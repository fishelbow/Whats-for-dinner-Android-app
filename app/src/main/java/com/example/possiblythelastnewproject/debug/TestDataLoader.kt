package com.example.possiblythelastnewproject.debug

import android.util.Log
import com.example.possiblythelastnewproject.features.pantry.data.PantryRepository
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.recipe.data.entities.Recipe
import com.example.possiblythelastnewproject.features.recipe.data.entities.RecipePantryItemCrossRef
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipePantryItemRepository
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipeRepository
import kotlinx.coroutines.flow.first


// TODO wire this in for a button, time to look up debug only buttons
suspend fun populateTestData(
    pantryRepo: PantryRepository,
    recipeRepo: RecipeRepository,
    crossRefRepo: RecipePantryItemRepository
) {
    val categories = listOf("Grains", "Vegetables", "Fruits", "Dairy", "Proteins", "Snacks", "Spices")

    // 1. Insert 5,000 PantryItems
    val pantryItems = (1..5000).map { i ->
        PantryItem(
            name = "Item $i",
            quantity = (0..10).random(),
            category = categories.random()
        )
    }

    pantryItems.chunked(500).forEach { chunk ->
        chunk.forEach { pantryRepo.insert(it) }
    }

    val allPantry = pantryRepo.getAllPantryItems().first()
    val pantryIdPool = allPantry.map { it.id }

    // 2. Insert 1,000 Recipes
    val recipes = (1..1000).map { i ->
        Recipe(
            name = "Recipe $i",
            temp = "350°F",
            prepTime = "15 min",
            cookTime = "30 min",
            category = categories.random(),
            instructions = "Step 1: Do something. Step 2: Eat."
        )
    }

    val recipeIds = mutableListOf<Long>()
    recipes.chunked(100).forEach { chunk ->
        chunk.forEach { recipe ->
            val id = recipeRepo.insert(recipe)
            recipeIds.add(id)
        }
    }

    // 3. Insert 50 cross-refs per recipe
    recipeIds.forEach { recipeId ->
        val refs = pantryIdPool.shuffled().take(50).map { pantryId ->
            RecipePantryItemCrossRef(
                recipeId = recipeId,
                pantryItemId = pantryId,
                required = true,
                amountNeeded = "${(1..5).random()}"
            )
        }
        crossRefRepo.replaceIngredientsForRecipe(recipeId, refs)
    }

    Log.d("TestData", "Inserted 5k pantry items, 1k recipes, and 50k cross-refs.")
}