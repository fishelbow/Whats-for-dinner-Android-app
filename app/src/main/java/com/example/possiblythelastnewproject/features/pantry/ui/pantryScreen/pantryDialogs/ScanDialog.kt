package com.example.possiblythelastnewproject.features.pantry.ui.pantryScreen.pantryDialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.pantry.domain.InlineBarcodeScanner

@Composable
fun ScanDialog(
    showScanDialog: Boolean,
    onDismiss: () -> Unit,
    pantryItems: List<PantryItem>,
    selectedItem: PantryItem?,
    onScanSuccess: (id: Int, scannedCode: String) -> Unit,
    onDuplicateScan: () -> Unit,
    onItemUpdate: (PantryItem) -> Unit
) {
    if (showScanDialog) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Scan Barcode")
                    Spacer(Modifier.height(12.dp))

                    InlineBarcodeScanner(
                        onResult = { scannedCode ->
                            val isDuplicate = pantryItems.any {
                                it.scanCode == scannedCode && it.id != selectedItem?.id
                            }

                            if (isDuplicate) {
                                onDuplicateScan()
                            } else {
                                val updatedItem = selectedItem?.copy(scanCode = scannedCode)
                                if (updatedItem != null) {
                                    onItemUpdate(updatedItem)
                                }
                                updatedItem?.let {
                                    onScanSuccess(it.id.toInt(), scannedCode)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f)
                    )
                }
            }
        }
    }
}