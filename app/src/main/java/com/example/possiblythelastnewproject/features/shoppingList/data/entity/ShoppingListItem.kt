package com.example.possiblythelastnewproject.features.shoppingList.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

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
        Index(value = ["uuid"], unique = true) // ✅ Enforce UUID uniqueness
    ]
)
data class ShoppingListItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listId: Long,
    val pantryItemId: Long,
    val name: String,
    val quantity: String,
    val isChecked: Boolean = false,
    val isGenerated: Boolean = false,
    val recipeId: Long? = null,
    val uuid: String = UUID.randomUUID().toString()
)