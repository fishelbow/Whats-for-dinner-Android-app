package com.example.possiblythelastnewproject.features.shoppingList.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(
    indices = [Index(value = ["listId", "recipeId"], unique = true), Index(value = ["uuid"], unique = true)]
)
data class RecipeSelection(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val listId: Long,
    val recipeId: Long,
    val count: Int,
    val uuid: String = UUID.randomUUID().toString()
)