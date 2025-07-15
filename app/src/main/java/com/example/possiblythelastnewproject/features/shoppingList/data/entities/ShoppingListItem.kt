package com.example.possiblythelastnewproject.features.shoppingList.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(
    tableName = "ShoppingListItem",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingList::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["listId"]),
        Index(value = ["uuid"], unique = true)
    ]
)
data class ShoppingListItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val listId: Long,                         // Link to parent ShoppingList
    val pantryItemId: Long?,                  // Optional reference to PantryItem
    val name: String,                         // Ingredient name
    val quantity: String,                     // Display quantity (e.g., "2 cups")
    val unit: String? = null,                 // Optional unit ("g", "pcs", etc.)
    val category: String = "",                // Category for display/grouping
    val isChecked: Boolean = false,           // Marked "found" by user
    val isGenerated: Boolean = false,         // True if auto-added by recipe
    val manuallyRemoved: Boolean = false,     // True if user manually deleted earlier
    val recipeId: Long? = null,               // Source recipe reference, if applicable
    val uuid: String = UUID.randomUUID().toString()
)