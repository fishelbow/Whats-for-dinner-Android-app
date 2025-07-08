package com.example.possiblythelastnewproject.debug

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.possiblythelastnewproject.features.pantry.data.PantryRepository
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipePantryItemRepository
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebugViewModel @Inject constructor(
    private val pantryRepo: PantryRepository,
    private val recipeRepo: RecipeRepository,
    private val crossRefRepo: RecipePantryItemRepository
) : ViewModel() {

    fun loadTestData() = viewModelScope.launch {
        try {
            populateTestData(pantryRepo, recipeRepo, crossRefRepo)
        } catch (e: Exception) {
            Log.e("DebugViewModel", "Failed to load test data", e)
        }
    }
}