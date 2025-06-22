package com.example.possiblythelastnewproject.features.recipe.data

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.possiblythelastnewproject.features.pantry.data.PantryItem
import com.example.possiblythelastnewproject.features.recipe.data.entities.Recipe
import com.example.possiblythelastnewproject.features.recipe.data.entities.RecipePantryItemCrossRef

data class RecipeWithIngredients(
    @Embedded val recipe: Recipe,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            RecipePantryItemCrossRef::class,
            parentColumn = "recipeId",
            entityColumn = "pantryItemId"
        )
    )
    val ingredients: List<PantryItem>
)