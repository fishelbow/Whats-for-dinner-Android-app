package com.example.possiblythelastnewproject.features.shoppingList.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ShoppingListItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val recipeId: Long? = null,       // If generated from a recipe
    val pantryItemId: Long? = null,   // If linked to a known pantry item
    val name: String,                 // Fallback or custom name
    val quantity: String = "",
    val isChecked: Boolean = false,   // User toggles when completed
    val isGenerated: Boolean = true   // Useful for distinguishing vs. user-added items
)