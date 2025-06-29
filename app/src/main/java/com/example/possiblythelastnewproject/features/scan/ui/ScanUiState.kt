package com.example.possiblythelastnewproject.features.scan.ui

import com.example.possiblythelastnewproject.features.pantry.data.entities.Category
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem

data class ScanUiState(
    val isLoading: Boolean = false,
    val scannedItem: PantryItem? = null,
    val scanSuccess: Boolean = false,
    val itemAdded: Boolean = false,
    val promptNewItemDialog: Boolean = false,
    val lastScanCode: String? = null,
    val promptLinkScanCodeDialog: Boolean = false,
    val scannerResetTrigger: Long = 0L,
    val selectedCategory: Category? = null
)