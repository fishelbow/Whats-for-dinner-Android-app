package com.example.possiblythelastnewproject.features.shoppingList.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(
    indices = [Index(value = ["uuid"], unique = true)]
)
data class UndoAction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val listId: Long,
    val actionType: String,             // e.g., "REMOVE_RECIPE", "ADD_INGREDIENT"
    val payload: String,                // Serialized JSON of affected data
    val timestamp: Long = System.currentTimeMillis(),
    val uuid: String = UUID.randomUUID().toString()
)