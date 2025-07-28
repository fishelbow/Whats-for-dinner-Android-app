package com.example.possiblythelastnewproject.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DbImportExportDialog(
    showDialog: Boolean,
    isLoading: Boolean,
    progress: Float,
    statusMessage: String?,
    onDismiss: () -> Unit,
    onImportClick: () -> Unit,
    onExportClick: () -> Unit
) {
    if (!showDialog) return

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Database Backup") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Export or import your entire database?")

                if (isLoading) {
                    statusMessage?.let { Text(it) }
                    LinearProgressIndicator(
                        progress =  progress ,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text("${(progress * 100).toInt()}% complete")
                }

                Button(onClick = onImportClick, enabled = !isLoading) {
                    Text("📥 Import DB")
                }
                Button(onClick = onExportClick, enabled = !isLoading) {
                    Text("📤 Export DB")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Close")
            }
        }
    )
}