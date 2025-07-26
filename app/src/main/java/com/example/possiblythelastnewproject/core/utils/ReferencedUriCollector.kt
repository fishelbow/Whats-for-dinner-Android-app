package com.example.possiblythelastnewproject.core.utils

import android.net.Uri
import com.example.possiblythelastnewproject.features.pantry.data.dao.PantryItemDao
import com.example.possiblythelastnewproject.features.recipe.data.dao.RecipeDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReferencedUriCollector @Inject constructor(
    private val pantryItemDao: PantryItemDao,
    private val recipeDao: RecipeDao
) {

    suspend fun collect(): Set<Uri> {
        val pantryUris = pantryItemDao.getAllPantryImageUris()
            .mapNotNull { it.toSafeUri() }

        val recipeUris = recipeDao.getAllRecipeImageUris()
            .mapNotNull { it.toSafeUri() }

        return (pantryUris + recipeUris).toSet()
    }
}