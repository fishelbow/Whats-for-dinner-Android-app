package com.example.possiblythelastnewproject.features.scan.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.possiblythelastnewproject.core.utils.compressImageFromUri
import kotlinx.coroutines.launch

@Composable
fun CreateFromScanDialog(
    scanCode: String,
    onConfirm: (String, Int, ByteArray?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            coroutineScope.launch {
                val compressed = compressImageFromUri(context, it, 300, 300, 80)
                imageBytes = compressed
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Item from Scan") },
        text = {
            Column {
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
                TextButton(
                    onClick = { imageLauncher.launch("image/*") },
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
            TextButton(onClick = {
                val qty = quantity.toIntOrNull() ?: 1
                if (name.isNotBlank()) onConfirm(name.trim(), qty, imageBytes)
            }) {
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