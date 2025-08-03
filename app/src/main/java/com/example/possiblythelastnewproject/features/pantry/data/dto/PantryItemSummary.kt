package com.example.possiblythelastnewproject.features.pantry.data.dto

data class PantryItemSummary(
    val id: Long,
    val name: String,
    val imageUri: String?,
    val scanCode: String?
)