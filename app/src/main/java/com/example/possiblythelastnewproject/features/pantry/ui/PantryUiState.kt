package com.example.possiblythelastnewproject.features.pantry.ui

import com.example.possiblythelastnewproject.features.pantry.data.Category
import com.example.possiblythelastnewproject.features.pantry.data.PantryItem

data class PantryUiState(
    val searchQuery: String = "",
    val showAddDialog: Boolean = false,
    val newIngredientName: String = "",
    val addImageBytes: ByteArray? = null,
    val editingItem: PantryItem? = null,
    val editName: String = "",
    val editQuantityText: String = "",
    val editImageBytes: ByteArray? = null,
    val itemToDelete: PantryItem? = null,
    val selectedCategory: Category? = null,
    val editCategory: Category? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PantryUiState

        if (showAddDialog != other.showAddDialog) return false
        if (searchQuery != other.searchQuery) return false
        if (newIngredientName != other.newIngredientName) return false
        if (addImageBytes != null) {
            if (other.addImageBytes == null) return false
            if (!addImageBytes.contentEquals(other.addImageBytes)) return false
        } else if (other.addImageBytes != null) return false
        if (editingItem != other.editingItem) return false
        if (editName != other.editName) return false
        if (editQuantityText != other.editQuantityText) return false
        if (editImageBytes != null) {
            if (other.editImageBytes == null) return false
            if (!editImageBytes.contentEquals(other.editImageBytes)) return false
        } else if (other.editImageBytes != null) return false
        if (itemToDelete != other.itemToDelete) return false
        if (selectedCategory != other.selectedCategory) return false
        if (editCategory != other.editCategory) return false

        return true
    }

    override fun hashCode(): Int {
        var result = showAddDialog.hashCode()
        result = 31 * result + searchQuery.hashCode()
        result = 31 * result + newIngredientName.hashCode()
        result = 31 * result + (addImageBytes?.contentHashCode() ?: 0)
        result = 31 * result + (editingItem?.hashCode() ?: 0)
        result = 31 * result + editName.hashCode()
        result = 31 * result + editQuantityText.hashCode()
        result = 31 * result + (editImageBytes?.contentHashCode() ?: 0)
        result = 31 * result + (itemToDelete?.hashCode() ?: 0)
        result = 31 * result + (selectedCategory?.hashCode() ?: 0)
        result = 31 * result + (editCategory?.hashCode() ?: 0)
        return result
    }
}