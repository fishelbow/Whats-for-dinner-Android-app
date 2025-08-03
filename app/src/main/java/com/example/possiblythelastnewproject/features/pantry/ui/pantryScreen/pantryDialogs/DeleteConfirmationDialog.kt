package com.example.possiblythelastnewproject.features.pantry.ui.pantryScreen.pantryDialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem

@Composable
fun DeleteConfirmationDialog(
    itemToDelete: PantryItem?,
    inUseIds: Set<Long>,
    onCancel: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    val item = itemToDelete ?: return
    val isInUse = item.id != 0L && inUseIds.contains(item.id)

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Delete Ingredient") },
        text = {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text("Are you sure you want to delete \"${item.name}\"?")
                if (isInUse) {
                    Spacer(modifier = Modifier.padding(top = 8.dp))
                    Text(
                        text = "This item is still used in one or more recipes.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onConfirmDelete() },
                enabled = !isInUse
            ) {
                Text("Yes, Delete")
            }
        }
    )
}