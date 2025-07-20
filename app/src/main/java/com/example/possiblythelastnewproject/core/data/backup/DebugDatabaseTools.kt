package com.example.possiblythelastnewproject.core.data.backup

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun DebugDatabaseTools(
    onRequestExport: () -> Unit,
    onRequestImport: () -> Unit
) {
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Database Tools") },
            text = { Text("Would you like to export or import the database?") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    onRequestExport()
                }) {
                    Text("Export")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    onRequestImport()
                }) {
                    Text("Import")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DebugDatabaseToolsPreview() {
    DebugDatabaseTools(
        onRequestExport = {},
        onRequestImport = {}
    )
}