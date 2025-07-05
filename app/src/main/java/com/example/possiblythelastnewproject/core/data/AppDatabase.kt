package com.example.possiblythelastnewproject.core.data


import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import com.example.possiblythelastnewproject.features.pantry.data.entities.Category
import com.example.possiblythelastnewproject.features.pantry.data.dao.CategoryDao
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.pantry.data.dao.PantryItemDao
import com.example.possiblythelastnewproject.features.recipe.data.dao.RecipeDao
import com.example.possiblythelastnewproject.features.recipe.data.dao.RecipePantryItemDao
import com.example.possiblythelastnewproject.features.recipe.data.entities.Recipe
import com.example.possiblythelastnewproject.features.recipe.data.entities.RecipePantryItemCrossRef
import com.example.possiblythelastnewproject.features.shoppingList.data.dao.ShoppingListDao
import com.example.possiblythelastnewproject.features.shoppingList.data.dao.ShoppingListEntryDao
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.ShoppingList
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.ShoppingListItem

@Database(
    entities = [
        Recipe::class,
        PantryItem::class,
        ShoppingList::class,
        ShoppingListItem::class,
        RecipePantryItemCrossRef::class,
        Category::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao
    abstract fun pantryItemDao(): PantryItemDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun recipePantryItemDao(): RecipePantryItemDao
    abstract fun categoryDao(): CategoryDao
    abstract fun shoppingListEntryDao(): ShoppingListEntryDao

    // a stub now helping to future proof any changes dont forget to bump up version number
    companion object {
        val migrations: Array<Migration> = arrayOf(
            // Add future migrations here, e.g., MIGRATION_1_2
        )
    }



}