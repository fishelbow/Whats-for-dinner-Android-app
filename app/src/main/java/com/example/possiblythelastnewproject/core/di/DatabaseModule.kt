package com.example.possiblythelastnewproject.core.di

import com.example.possiblythelastnewproject.core.data.AppDatabase
import android.content.Context
import androidx.room.Room
import com.example.possiblythelastnewproject.features.recipe.data.dao.RecipeDao
import com.example.possiblythelastnewproject.features.pantry.data.dao.CategoryDao
import com.example.possiblythelastnewproject.features.pantry.data.dao.PantryItemDao
import com.example.possiblythelastnewproject.features.recipe.data.dao.RecipePantryItemDao
import com.example.possiblythelastnewproject.features.shoppingList.data.dao.RecipeSelectionDao
import com.example.possiblythelastnewproject.features.shoppingList.data.dao.ShoppingListDao
import com.example.possiblythelastnewproject.features.shoppingList.data.dao.ShoppingListEntryDao
import com.example.possiblythelastnewproject.features.shoppingList.data.dao.ShoppingListItemDao
import com.example.possiblythelastnewproject.features.shoppingList.data.dao.UndoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "PossiblyTheLastNewProject"
        )
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides fun provideRecipeDao(db: AppDatabase): RecipeDao = db.recipeDao()
    @Provides fun providePantryItemDao(db: AppDatabase): PantryItemDao = db.pantryItemDao()
    @Provides fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideRecipePantryItemDao(db: AppDatabase): RecipePantryItemDao = db.recipePantryItemDao()
    @Provides fun provideShoppingListDao(db: AppDatabase): ShoppingListDao = db.shoppingListDao()
    @Provides fun provideShoppingListEntryDao(db: AppDatabase): ShoppingListEntryDao = db.shoppingListEntryDao()
    @Provides fun provideShoppingListItemDao(db: AppDatabase): ShoppingListItemDao = db.shoppingListItemDao()
    @Provides fun provideRecipeSelectionDao(db: AppDatabase): RecipeSelectionDao = db.recipeSelectionDao()
    @Provides fun provideUndoDao(db: AppDatabase): UndoDao = db.undoDao()



    @Provides
    fun provideDefaultCategoryNames(): List<String> = listOf(
        "Produce", "Snacks", "Dairy", "Meat", "Grains", "Sauce",
        "Frozen", "Drinks", "Paper goods", "Canned goods",
        "Spices", "Toiletries", "Other"
    )
}