package com.example.possiblythelastnewproject.features.scan.ui.componets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.possiblythelastnewproject.core.utils.imagePicker

@Composable
fun CreateFromScanDialog(
    scanCode: String,
    onConfirm: (String, Int, ByteArray?) -> Unit,
    onDismiss: () -> Unit
) {
    // Local state for name, quantity, and image bytes.
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }

    // Use your universal image picker. It returns a lambda;
    // here we update imageBytes when the user picks an image.
    val launchImagePicker = imagePicker { newBytes ->
        imageBytes = newBytes
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Item from Scan") },
        text = {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter(Char::isDigit) },
                    label = { Text("Quantity") },
                    singleLine = true,
                    modifier = Modifier.padding(top = 8.dp)
                )
                // Use the universal image picker:
                TextButton(
                    onClick = { launchImagePicker() },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Pick Image")
                }
                Text(
                    text = "Scan Code: $scanCode",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val qty = quantity.toIntOrNull() ?: 1
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), qty, imageBytes)
                    }
                }
            ) {
                Text("Add to Pantry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}