package com.example.possiblythelastnewproject.features.recipe.ui

import com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeDetail.RecipeEditUiState
import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.possiblythelastnewproject.core.utils.deleteImageFromStorage
import com.example.possiblythelastnewproject.features.recipe.data.RecipeWithIngredients
import com.example.possiblythelastnewproject.features.recipe.data.dao.RecipeDao
import com.example.possiblythelastnewproject.features.recipe.data.entities.Recipe
import com.example.possiblythelastnewproject.features.recipe.data.entities.RecipePantryItemCrossRef
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipePantryItemRepository
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipeRepository
import com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeCreation.RecipeIngredientUI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipesViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    val ingredientRepository: RecipePantryItemRepository,
    private val recipeDao: RecipeDao
) : ViewModel() {

    // 🧠 UI Edit State
    val State = MutableStateFlow(RecipeEditUiState())
    val uiState: StateFlow<RecipeEditUiState> = State.asStateFlow()

    inline fun updateUi(transform: RecipeEditUiState.() -> RecipeEditUiState) {
        State.update { it.transform() }
    }

    // 🖍 Field Updaters
    fun updateCardColor(color: Color) = updateUi { copy(cardColor = color) }
    fun updateIngredients(list: List<RecipeIngredientUI>) = updateUi { copy(ingredients = list) }

    fun commitImageUri() = updateUi { commitImage() }
    fun rollbackImageUri() {
        val restored = State.value.lastStableImageUri ?: return

        updateUi {
            copy(
                pendingImageUris = listOf(restored),
                currentImageIndex = 0,
                preservedImageUri = restored // 👈 Marks this image for cleanup protection
            )
        }
    }
    var activeRecipeId by mutableStateOf<Long?>(null)

    // 📦 Recipe Flows
    val allRecipes: StateFlow<List<Recipe>> =
        recipeRepository.getAllRecipes()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recipes: StateFlow<List<RecipeWithIngredients>> =
        recipeRepository.getRecipesWithIngredients()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 🧭 Snapshot Loader
    fun loadRecipe(recipeId: Long) = viewModelScope.launch {
        val snapshot = recipeRepository.getRecipeWithIngredients(recipeId) ?: return@launch
        val crossRefs = ingredientRepository.getCrossRefsForRecipeOnce(recipeId)
        State.value = RecipeEditUiState.snapshotFrom(snapshot, crossRefs)
    }

    // 🕵️ Name Collision
    suspend fun recipeNameExists(name: String, excludeUuid: String? = null): Boolean {
        val trimmed = name.trim()
        return if (excludeUuid != null)
            recipeDao.existsByNameExcludingUuid(trimmed, excludeUuid)
        else
            recipeDao.existsByName(trimmed)
    }
/*
    // 🧼 Image Cleanup
    fun discardImagesIfNeeded(context: Context): Boolean {
        val state = State.value

        // 🛡️ Always protect currently displayed image
        val protected = buildSet {
            state.imageUri?.let { add(it) }
            state.pendingImageUris.getOrNull(state.currentImageIndex)?.let { add(it) }
            state.preservedImageUri?.let { add(it) } // ✅ Protect explicitly restored URI
        }

        val deletable = state.pendingImageUris
            .filter { uri -> !protected.contains(uri) }

        deletable.forEach {
            if (protected.contains(it)) {
                Log.w("ImageCleanup", "⚠️ Skipping delete of protected image: $it")
            } else {
                deleteImageFromStorage(it, context)
            }
        }

        updateUi { rollbackImages().copy(preservedImageUri = null) }
        return deletable.isNotEmpty()
    }
*/
    // 💾 Save New Recipe
    fun saveRecipeWithIngredientsUi(
        recipe: Recipe,
        ingredients: List<RecipeIngredientUI>,
        context: Context
    ) = viewModelScope.launch {
        val state = State.value
        state.pendingImageUris
            .filterIndexed { i, uri -> i != state.currentImageIndex && uri != state.imageUri }
            .forEach { deleteImageFromStorage(it, context) }

        commitImageUri()

        val recipeId = recipeRepository.insert(recipe.copy(id = 0L))
        val crossRefs = ingredients.filter { it.pantryItemId != null }.map {
            RecipePantryItemCrossRef(
                recipeId = recipeId,
                pantryItemId = it.pantryItemId!!,
                required = it.required,
                amountNeeded = it.amountNeeded
            )
        }
        crossRefs.forEach { ingredientRepository.insertCrossRef(it) }
    }

    // 💾 Update Existing Recipe
    fun updateRecipeWithIngredientsUi(
        updatedRecipe: Recipe,
        updatedIngredients: List<RecipeIngredientUI>,
        context: Context
    ) = viewModelScope.launch {
        val state = State.value

        state.pendingImageUris
            .filterIndexed { i, uri -> i != state.currentImageIndex && uri != state.imageUri }
            .forEach { deleteImageFromStorage(it, context) }

        commitImageUri()

        recipeRepository.insert(updatedRecipe)

        val refs = updatedIngredients.filter { it.pantryItemId != null }.map {
            RecipePantryItemCrossRef(
                recipeId = updatedRecipe.id,
                pantryItemId = it.pantryItemId!!,
                required = it.required,
                amountNeeded = it.amountNeeded
            )
        }

        ingredientRepository.replaceIngredientsForRecipe(updatedRecipe.id, refs)
    }

    // 🗑 Delete Recipe
    fun deleteRecipe(recipe: Recipe, context: Context) = viewModelScope.launch {
        State.value.pendingImageUris.forEach { deleteImageFromStorage(it, context) }

        recipeRepository.delete(recipe, context)
        ingredientRepository.deleteCrossRefsForRecipe(recipe.id)

        updateUi {
            if (imageUri == recipe.imageUri) {
                copy(imageUri = null, pendingImageUris = emptyList(), currentImageIndex = -1)
            } else this
        }
    }

    // 🔄 Restore From Snapshot
    fun restoreRecipeState(
        restoredRecipe: Recipe,
        restoredIngredients: List<RecipeIngredientUI>
    ) = viewModelScope.launch {
        recipeRepository.insert(restoredRecipe)

        val refs = restoredIngredients.filter { it.pantryItemId != null }.map {
            RecipePantryItemCrossRef(
                recipeId = restoredRecipe.id,
                pantryItemId = it.pantryItemId!!,
                required = it.required,
                amountNeeded = it.amountNeeded
            )
        }

        ingredientRepository.replaceIngredientsForRecipe(restoredRecipe.id, refs)
    }

    // 🎯 Snapshot Getter
    suspend fun getRecipeWithIngredients(recipeId: Long): RecipeWithIngredients? {
        return recipeRepository.getRecipeWithIngredients(recipeId)
    }

    // 🖼 Image Switching + Cleanup
    fun replaceImageUri(uri: String, context: Context) {
        commitImageUri() // 💾 ensure imageUri is finalized

        val state = uiState.value
        val deletable = state.pendingImageUris
            .filterNot { it == uri || it == state.imageUri }

        deletable.forEach { deleteImageFromStorage(it, context) }

        updateUi { copy(pendingImageUris = listOf(uri), currentImageIndex = 0) }
    }

    fun removeOrphanedImages(
        context: Context,
        allRecipes: List<Recipe>,
        uiState: RecipeEditUiState
    ): List<String> {
        val filesDir = context.filesDir
        val allStoredImages = filesDir.listFiles()
            ?.filter { it.name.endsWith(".jpg") || it.name.endsWith(".png") }
            ?: emptyList()

        // ✅ This is where your validFilenames set goes:
        val validFilenames = buildSet {
            allRecipes.mapNotNull { it.imageUri?.substringAfterLast("/") }.forEach { add(it) }
            uiState.imageUri?.substringAfterLast("/")?.let { add(it) }
            uiState.pendingImageUris.map { it.substringAfterLast("/") }.forEach { add(it) }
        }

        val orphaned = allStoredImages.filterNot { file ->
            validFilenames.contains(file.name)
        }

        orphaned.forEach {
            val deleted = deleteImageFromStorage(it.absolutePath, context)
            if (deleted) Log.d("StorageCleanup", "🧹 Deleted: ${it.name}")
            else Log.w("StorageCleanup", "❌ Failed to delete: ${it.name}")
        }

        return orphaned.map { it.absolutePath }
    }
    private val queryFlow = MutableStateFlow("") // Holds current search query

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val pagedRecipes: Flow<PagingData<RecipeWithIngredients>> =
        queryFlow
            .debounce(300)
            .distinctUntilChanged()
            .flatMapLatest { query ->
                Pager(
                    config = PagingConfig(pageSize = 20),
                    pagingSourceFactory = { recipeDao.getPagedRecipesFiltered(query) }
                ).flow.cachedIn(viewModelScope)
            }

    fun updateQuery(newQuery: String) {
        queryFlow.value = newQuery
    }


}