package com.example.possiblythelastnewproject.features.pantry.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.possiblythelastnewproject.core.utils.deleteImageFromStorage
import com.example.possiblythelastnewproject.features.pantry.data.entities.Category
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.pantry.data.PantryRepository
import com.example.possiblythelastnewproject.features.pantry.data.dao.PantryItemDao
import com.example.possiblythelastnewproject.features.recipe.data.dao.RecipePantryItemDao
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipePantryItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class PantryViewModel @Inject constructor(
    private val repository: PantryRepository,
    private val pantryItemDao: PantryItemDao


) : ViewModel() {

    fun toggleShoppingStatus(itemId: Long, context: Context) {
        viewModelScope.launch {
            val item = pantryItems.value.firstOrNull { it.id == itemId } ?: return@launch
            val updated = item.copy(addToShoppingList = !item.addToShoppingList)
            repository.update(
                updated,
                oldImageUri = item.imageUri ?: "",
                context = context
            )
        }
    }

    init {
        viewModelScope.launch {
            repository.populateDefaultCategoriesIfEmpty()
        }
    }

    val allCategories: StateFlow<List<Category>> = repository
        .getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // produce a live set of all pantryItemIds used in recipes
    val inUsePantryItemIds: StateFlow<Set<Long>> =
        repository.observeAllCrossRefs()
            .map { it.map { ref -> ref.pantryItemId }.toSet() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun update(item: PantryItem, oldImageUri: String, context: Context) {
        viewModelScope.launch {
            // 🧹 Clean up old image if replaced
            if (item.imageUri != oldImageUri) {
                deleteImageFromStorage(oldImageUri, context)
            }

            //  Update DB
            repository.update(
                item,
                oldImageUri = oldImageUri,
                context = context
            )
        }
    }
        // Reactive stream of all pantry items
        val pantryItems: StateFlow<List<PantryItem>> = repository
            .getAllPantryItems()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Public alias for composable
        val allItems: StateFlow<List<PantryItem>> = pantryItems

        // Internal UI state
        private val _uiState = MutableStateFlow(PantryUiState())
        val uiState: StateFlow<PantryUiState> = _uiState.asStateFlow()

        // — UI State modifiers

    fun updateSelectedCategory(category: Category?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun updateEditCategory(category: Category?) {
        _uiState.update { it.copy(editCategory = category) }
    }


    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun startEditing(item: PantryItem?) {
        item?.let {
            _uiState.update {
                it.copy(
                    editingItem = item,
                    editName = item.name,
                    editQuantityText = item.quantity.toString(),
                    editImageUri = item.imageUri,
                    editCategory = allCategories.value.firstOrNull { cat -> cat.name == item.category }
                )
            }
        }
    }

    fun confirmEditItem(context: Context) = viewModelScope.launch {
        val state = _uiState.value
        val original = state.editingItem ?: return@launch
        val newQuantity = state.editQuantityText.toIntOrNull() ?: original.quantity

        val updatedItem = original.copy(
            name = state.editName.trim(),
            quantity = newQuantity,
            imageUri = state.editImageUri,
            category = state.editCategory?.name ?: original.category
        )

        //  Update DB
        repository.update(
            updatedItem,
            oldImageUri = original.imageUri ?: "",
            context = context
        )

        _uiState.update { it.copy(editingItem = null) }
    }


    fun updateEditFields(name: String, quantity: String) {
        _uiState.update {
            it.copy(editName = name, editQuantityText = quantity)
        }
    }

    fun updateEditImage(uri: Uri?) {
        _uiState.update { it.copy(editImageUri = uri?.toString()) }
    }


    fun promptDelete(item: PantryItem) {
            _uiState.update { it.copy(itemToDelete = item) }
        }

    fun cancelDelete() {
            _uiState.update { it.copy(itemToDelete = null) }
        }

    fun confirmDelete(context: Context) = viewModelScope.launch {
        uiState.value.itemToDelete?.let { item ->

            // 🧹 Clean up uncommitted edit image if it exists
            val editDraftUri = uiState.value.editImageUri
            if (editDraftUri != null && editDraftUri != item.imageUri) {
                deleteImageFromStorage(editDraftUri, context)
            }

            // 💥 Delete pantry item (and confirmed image)
            repository.delete(item, context = context)

            val files = context.filesDir.listFiles()
            Log.d("ImageCleanup", "Remaining images: ${files?.map { it.name }}")
        }

        _uiState.update { it.copy(itemToDelete = null, editingItem = null, editImageUri = null) }
    }

    fun insertAndReturn(name: String): PantryItem = runBlocking {
            val trimmed = name.trim()
            pantryItems.value.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
                ?: run {
                    val newItem = PantryItem(name = trimmed, quantity = 0)
                    val id = repository.insert(newItem)
                    newItem.copy(id = id)
                }
        }

    fun addPantryItem(pantryItem: PantryItem) {
        viewModelScope.launch {
            // Prevent duplicates by name (ignoring case)
            val nameExists = pantryItems.value.any {
                it.name.equals(pantryItem.name.trim(), ignoreCase = true)
            }
            if (!nameExists && pantryItem.name.isNotBlank()) {
                repository.insert(pantryItem)
            }
        }
    }
    fun updateScanCode(id: Long, scannedCode: String, context: Context) {
        viewModelScope.launch {
            val duplicate = pantryItems.value.any { it.scanCode == scannedCode && it.id != id }
            if (duplicate) {
                Log.w("PantryViewModel", "Duplicate PLU/Barcode: $scannedCode already used.")
                return@launch
            }

            pantryItems.value.find { it.id == id }?.let { item ->
                val updated = item.copy(scanCode = scannedCode)

                repository.update(
                    updated,
                    oldImageUri = item.imageUri ?: "",
                    context = context
                )
            }
        }
    }

    fun clearAddImageUri() {
        _uiState.update { it.copy(addImageUri = null) }
    }

    fun updateAddImage(uri: Uri?) {
        _uiState.update { it.copy(addImageUri = uri?.toString()) }
    }

    private var pendingPantryImagePath: String? = null

    fun setPendingPantryImagePath(path: String) {
        pendingPantryImagePath = path
    }

    fun swapAddImage(context: Context, newUri: Uri?) {
        val previous = _uiState.value.addImageUri
        if (previous != null && previous != newUri?.toString()) {
            deleteImageFromStorage(previous, context)
        }
        _uiState.update { it.copy(addImageUri = newUri?.toString()) }
    }

    fun swapEditImage(context: Context, newUri: Uri?) {
        val previous = _uiState.value.editImageUri
        if (previous != null && previous != newUri?.toString()) {
            deleteImageFromStorage(previous, context)
        }
        _uiState.update { it.copy(editImageUri = newUri?.toString()) }
    }

    fun auditAndCleanOrphans(context: Context) {
        val referencedUris = pantryItems.value
            .mapNotNull { it.imageUri }
            .toSet()

        PantryImageCleaner.cleanUnreferencedImages(context, referencedUris)
    }
}
