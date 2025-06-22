package com.example.possiblythelastnewproject.features.scan.ui

import com.example.possiblythelastnewproject.features.pantry.data.PantryItem

data class ScanUiState(
    val isLoading: Boolean = false,
    val scannedItem: PantryItem? = null,
    val scanSuccess: Boolean = false,
    val itemAdded: Boolean = false,
    val promptNewItemDialog: Boolean = false,
    val lastScanCode: String? = null
)