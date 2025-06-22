package com.example.possiblythelastnewproject.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.possiblythelastnewproject.features.pantry.data.PantryItemDao
import com.example.possiblythelastnewproject.features.recipe.data.entities.Recipe
import com.example.possiblythelastnewproject.features.recipe.data.entities.RecipePantryItemCrossRef
import com.example.possiblythelastnewproject.features.recipe.data.dao.RecipePantryItemDao
import com.example.possiblythelastnewproject.features.shoppingList.data.ShoppingListDao
import com.example.possiblythelastnewproject.features.shoppingList.data.ShoppingListItem
import com.example.possiblythelastnewproject.features.recipe.data.dao.RecipeDao
import com.example.possiblythelastnewproject.features.pantry.data.PantryItem

@Database(
    entities = [
        Recipe::class,
        PantryItem::class,
        ShoppingListItem::class,
        RecipePantryItemCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao
    abstract fun pantryItemDao(): PantryItemDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun recipeIngredientDao(): RecipePantryItemDao
}