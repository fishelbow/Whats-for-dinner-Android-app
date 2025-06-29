package com.example.possiblythelastnewproject.core.di

import com.example.possiblythelastnewproject.core.data.AppDatabase
import android.content.Context
import androidx.room.Room
import com.example.possiblythelastnewproject.features.recipe.data.dao.RecipeDao
import com.example.possiblythelastnewproject.features.pantry.data.dao.CategoryDao
import com.example.possiblythelastnewproject.features.pantry.data.dao.PantryItemDao
import com.example.possiblythelastnewproject.features.recipe.data.dao.RecipePantryItemDao
import com.example.possiblythelastnewproject.features.shoppingList.data.dao.ShoppingListDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // Provide the Room Database instance
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "PossiblyTheLastNewProject_database"
        )
            .fallbackToDestructiveMigration(false) // Adjust migration as needed
            .build()
    }

    // Provide RecipeDao instance
    @Provides
    fun provideRecipeDao(appDatabase: AppDatabase): RecipeDao {
        return appDatabase.recipeDao()
    }

    // Provide PantryItemDao instance
    @Provides
    fun providePantryItemDao(appDatabase: AppDatabase): PantryItemDao {
        return appDatabase.pantryItemDao()
    }

    // Provide ShoppingListDao instance
    @Provides
    fun provideShoppingListDao(appDatabase: AppDatabase): ShoppingListDao {
        return appDatabase.shoppingListDao()
    }

    // Provide RecipePantryItemDao instance
    @Provides
    fun provideRecipePantryItemDao(appDatabase: AppDatabase): RecipePantryItemDao {
        return appDatabase.recipeIngredientDao()
    }

    @Provides
    fun provideCategoryDao(appDatabase: AppDatabase): CategoryDao {
        return appDatabase.categoryDao()
    }

    // Provide CategoryDao instance
    @Provides
    fun provideDefaultCategoryNames(): List<String> = listOf(
        "Grains",
        "Vegetables",
        "Fruits",
        "Dairy",
        "Protein",
        "Snacks",
        "Spices",
        "Baking",
        "Condiments",
        "Beverages"
    )
}