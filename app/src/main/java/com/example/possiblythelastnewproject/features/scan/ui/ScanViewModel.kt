package com.example.possiblythelastnewproject.features.scan.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.possiblythelastnewproject.features.pantry.data.entities.Category
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.scan.data.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val repository: ScanRepository
) : ViewModel() {
    private var pendingItemExtras: Triple<Int, ByteArray?, String>? = null


    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    val allCategories: StateFlow<List<Category>> = repository
        .getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateSelectedCategory(category: Category?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun scan(code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, itemAdded = false) }

            val scanMatch = repository.findByScanCode(code)
            if (scanMatch != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        scannedItem = scanMatch,
                        scanSuccess = true,
                        promptNewItemDialog = false,
                        promptLinkScanCodeDialog = false,
                        lastScanCode = code
                    )
                }
            } else {
                val nameMatch = repository.findByName(code)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        scannedItem = nameMatch,
                        scanSuccess = false,
                        promptNewItemDialog = nameMatch == null,
                        promptLinkScanCodeDialog = nameMatch != null,
                        lastScanCode = code
                    )
                }
            }
        }
    }
    fun addItem(item: PantryItem) = viewModelScope.launch {
        repository.insert(item)
        _uiState.update { it.copy(itemAdded = true) }
        clearScanResult()
    }

    fun updateItem(item: PantryItem) = viewModelScope.launch {
        repository.insert(item) // OnConflictStrategy.REPLACE will update it
        _uiState.update { it.copy(itemAdded = true, scannedItem = null, scanSuccess = false) }
    }

    fun clearScanResult() {
        _uiState.update {
            it.copy(
                isLoading = false,
                scannedItem = null,
                scanSuccess = false,
                promptNewItemDialog = false,
                promptLinkScanCodeDialog = false,
                lastScanCode = null,
                itemAdded = false,
                scannerResetTrigger = System.currentTimeMillis()
            )
        }
    }

    fun handleManualNameEntry(
        name: String,
        pendingScanCode: String,
        quantity: Int,
        imageData: ByteArray?
    ) = viewModelScope.launch {
        val existingItem = repository.findByName(name.trim())
        if (existingItem != null) {
            pendingItemExtras = Triple(quantity, imageData, name)
            _uiState.update {
                it.copy(
                    scannedItem = existingItem,
                    lastScanCode = pendingScanCode,
                    promptLinkScanCodeDialog = true
                )
            }
        } else {
            val newItem = PantryItem(
                name = name.trim(),
                quantity = quantity,
                scanCode = pendingScanCode,
                imageData = imageData
            )
            addItem(newItem)
        }
    }


    fun linkScanCodeToItem(item: PantryItem, scanCode: String) = viewModelScope.launch {
        val (qty, image, _) = pendingItemExtras ?: Triple(0, null, "")
        val updated = item.copy(
            scanCode = scanCode,
            quantity = item.quantity + qty,
            imageData = item.imageData ?: image
        )
        repository.updateItem(updated)
        _uiState.update {
            it.copy(
                scannedItem = null,
                scanSuccess = false,
                promptLinkScanCodeDialog = false,
                promptNewItemDialog = false,  // 👈 add this line
                itemAdded = true
            )
        }
        pendingItemExtras = null
    }

    fun addPantryItem(item: PantryItem) = viewModelScope.launch {
        repository.insert(item)
        _uiState.update { it.copy(itemAdded = true) }
        clearScanResult()
    }
}