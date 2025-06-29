package com.example.possiblythelastnewproject.features.shoppingList.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    indices = [Index(value = ["uuid"], unique = true)]
)
data class ShoppingList(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val uuid: String = UUID.randomUUID().toString()
)