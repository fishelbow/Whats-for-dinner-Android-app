package com.example.possiblythelastnewproject.features.scan.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.possiblythelastnewproject.features.pantry.data.PantryItem
import com.example.possiblythelastnewproject.features.scan.data.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val repository: ScanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun scan(code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, itemAdded = false) }
            val match = repository.findByScanCode(code)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    scannedItem = match,
                    scanSuccess = match != null,
                    promptNewItemDialog = match == null,
                    lastScanCode = code
                )
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
                lastScanCode = null,
                itemAdded = false
            )
        }
    }
}