package com.example.possiblythelastnewproject.features.pantry.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    indices = [Index(value = ["uuid"], unique = true)]
)
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val uuid: String = UUID.randomUUID().toString()
)