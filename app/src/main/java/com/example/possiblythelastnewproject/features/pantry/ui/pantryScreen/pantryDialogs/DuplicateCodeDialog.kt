package com.example.possiblythelastnewproject.features.pantry.ui.pantryScreen.pantryDialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun DuplicateCodeDialog(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    if (visible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("OK")
                }
            },
            title = { Text("Duplicate Code") },
            text = {
                Text("That scan code is already linked to another item.")
            }
        )
    }
}