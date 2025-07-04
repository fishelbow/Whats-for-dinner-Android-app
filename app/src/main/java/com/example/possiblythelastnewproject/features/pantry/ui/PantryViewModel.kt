package com.example.possiblythelastnewproject.features.pantry.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.possiblythelastnewproject.features.pantry.data.entities.Category
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.pantry.data.PantryRepository
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipePantryItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val repository: PantryRepository

) : ViewModel() {

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

    fun update(item: PantryItem) = viewModelScope.launch {
        repository.update(item)
    }
        // Reactive stream of all pantry items
        val pantryItems: StateFlow<List<PantryItem>> = repository
            .getAllPantryItems()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Public alias for composables
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
                        editImageBytes = item.imageData,
                        editCategory = allCategories.value.firstOrNull { cat -> cat.name == item.category }
                    )

                }
            }
        }

        fun updateEditFields(name: String, quantity: String) {
            _uiState.update {
                it.copy(editName = name, editQuantityText = quantity)
            }
        }

        fun updateEditImage(bytes: ByteArray?) {
            _uiState.update { it.copy(editImageBytes = bytes) }
        }

    fun confirmEditItem() = viewModelScope.launch {
        val state = _uiState.value
        val original = state.editingItem ?: return@launch
        val newQuantity = state.editQuantityText.toIntOrNull() ?: original.quantity

        val updatedItem = original.copy(
            name = state.editName.trim(),
            quantity = newQuantity,
            imageData = state.editImageBytes,
            category = state.editCategory?.name ?: original.category
        )
        repository.update(updatedItem)
        _uiState.update { it.copy(editingItem = null) }
    }

        fun promptDelete(item: PantryItem) {
            _uiState.update { it.copy(itemToDelete = item) }
        }

        fun cancelDelete() {
            _uiState.update { it.copy(itemToDelete = null) }
        }

        fun confirmDelete() = viewModelScope.launch {
            uiState.value.itemToDelete?.let { repository.delete(it) }
            _uiState.update { it.copy(itemToDelete = null, editingItem = null) }
        }

        fun insertAndReturn(name: String): PantryItem = runBlocking {
            val trimmed = name.trim()
            pantryItems.value.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
                ?: run {
                    val newItem = PantryItem(name = trimmed, quantity = 1)
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
    fun updateScanCode(id: Long, scannedCode: String) {
        viewModelScope.launch {
            val duplicate = pantryItems.value.any { it.scanCode == scannedCode && it.id != id }
            if (duplicate) {
                Log.w("PantryViewModel", "Duplicate PLU/Barcode: $scannedCode already used.")
                // Optional: emit a Snackbar, dialog, or update UI state to notify
                return@launch
            }

            pantryItems.value.find { it.id == id }?.let { item ->
                repository.update(item.copy(scanCode = scannedCode))
            }
        }
    }


}
