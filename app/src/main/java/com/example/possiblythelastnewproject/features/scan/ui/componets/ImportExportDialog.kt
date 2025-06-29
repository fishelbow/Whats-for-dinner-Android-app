package com.example.possiblythelastnewproject.features.scan.ui.componets

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ImportExportDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onImportClick: () -> Unit,
    onExportClick: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Debug Tools") },
            text = { Text("Would you like to import or export your data?") },
            confirmButton = {
                TextButton(onClick = onExportClick) {
                    Text("Export")
                }
            },
            dismissButton = {
                TextButton(onClick = onImportClick) {
                    Text("Import")
                }
            }
        )
    }
}