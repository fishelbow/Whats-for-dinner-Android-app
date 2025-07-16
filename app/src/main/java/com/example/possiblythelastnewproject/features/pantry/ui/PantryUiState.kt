package com.example.possiblythelastnewproject.features.pantry.ui

import com.example.possiblythelastnewproject.features.pantry.data.entities.Category
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem

data class PantryUiState(
    val searchQuery: String = "",
    val showAddDialog: Boolean = false,
    val newIngredientName: String = "",
    val addImageUri: String? = null,
    val editingItem: PantryItem? = null,
    val editName: String = "",
    val editQuantityText: String = "",
    val editImageUri: String? = null,
    val itemToDelete: PantryItem? = null,
    val selectedCategory: Category? = null,
    val editCategory: Category? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PantryUiState) return false

        return searchQuery == other.searchQuery &&
                showAddDialog == other.showAddDialog &&
                newIngredientName == other.newIngredientName &&
                addImageUri == other.addImageUri &&
                editingItem == other.editingItem &&
                editName == other.editName &&
                editQuantityText == other.editQuantityText &&
                editImageUri == other.editImageUri &&
                itemToDelete == other.itemToDelete &&
                selectedCategory == other.selectedCategory &&
                editCategory == other.editCategory
    }

    override fun hashCode(): Int {
        var result = showAddDialog.hashCode()
        result = 31 * result + searchQuery.hashCode()
        result = 31 * result + newIngredientName.hashCode()
        result = 31 * result + (addImageUri?.hashCode() ?: 0)
        result = 31 * result + (editingItem?.hashCode() ?: 0)
        result = 31 * result + editName.hashCode()
        result = 31 * result + editQuantityText.hashCode()
        result = 31 * result + (editImageUri?.hashCode() ?: 0)
        result = 31 * result + (itemToDelete?.hashCode() ?: 0)
        result = 31 * result + (selectedCategory?.hashCode() ?: 0)
        result = 31 * result + (editCategory?.hashCode() ?: 0)
        return result
    }
}