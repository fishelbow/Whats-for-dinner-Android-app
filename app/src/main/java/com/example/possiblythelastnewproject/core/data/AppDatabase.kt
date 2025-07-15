package com.example.possiblythelastnewproject.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import com.example.possiblythelastnewproject.features.pantry.data.dao.CategoryDao
import com.example.possiblythelastnewproject.features.pantry.data.dao.PantryItemDao
import com.example.possiblythelastnewproject.features.pantry.data.entities.Category
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.recipe.data.dao.RecipeDao
import com.example.possiblythelastnewproject.features.recipe.data.dao.RecipePantryItemDao
import com.example.possiblythelastnewproject.features.recipe.data.entities.Recipe
import com.example.possiblythelastnewproject.features.recipe.data.entities.RecipePantryItemCrossRef
import com.example.possiblythelastnewproject.features.shoppingList.data.dao.*
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.*

@Database(
    entities = [
        Recipe::class,
        PantryItem::class,
        ShoppingList::class,
        ShoppingListItem::class,
        RecipePantryItemCrossRef::class,
        Category::class,
        RecipeSelection::class,
        UndoAction::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    // 🥫 Pantry
    abstract fun pantryItemDao(): PantryItemDao
    abstract fun categoryDao(): CategoryDao

    // 🍽️ Recipes
    abstract fun recipeDao(): RecipeDao
    abstract fun recipePantryItemDao(): RecipePantryItemDao

    // 🛒 Shopping List
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun shoppingListItemDao(): ShoppingListItemDao
    abstract fun shoppingListEntryDao(): ShoppingListEntryDao
    abstract fun recipeSelectionDao(): RecipeSelectionDao
    abstract fun undoDao(): UndoDao

    companion object {
        // 🛠️ Migration stub — bump version when adding schema changes
        val migrations: Array<Migration> = arrayOf(
            // Future example: MIGRATION_1_2
        )
    }
}