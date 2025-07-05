package com.example.possiblythelastnewproject.features.scan.ui.componets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.possiblythelastnewproject.core.utils.imagePicker
import com.example.possiblythelastnewproject.features.pantry.data.entities.Category
import com.example.possiblythelastnewproject.features.pantry.ui.PantryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFromScanDialog(
    scanCode: String,
    categories: List<Category>,
    selectedCategory: Category?,
    onCategoryChange: (Category) -> Unit,
    onConfirm: (String, Int, ByteArray?, String?) -> Unit,
    onDismiss: () -> Unit
) {
    val pantryViewModel: PantryViewModel = hiltViewModel()
    val pantryItems by pantryViewModel.pantryItems.collectAsState()

    var showDuplicateDialog by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    val launchImagePicker = imagePicker { newBytes -> imageBytes = newBytes }

    AlertDialog(
        onDismissRequest = {},
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
                TextButton(
                    onClick = { launchImagePicker() },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Pick Image")
                }

                if (imageBytes != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageBytes)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                            .height(180.dp) // adjust as needed
                    )
                }

                Spacer(Modifier.padding(top = 8.dp))

                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)

                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    onCategoryChange(category)
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
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
                    val trimmedName = name.trim()
                    val qty = quantity.toIntOrNull() ?: 1

                    val nameExists = pantryItems.any { it.name.equals(trimmedName, ignoreCase = true) }

                    if (trimmedName.isBlank()) return@TextButton

                    if (nameExists) {
                        showDuplicateDialog = true
                    } else {
                        onConfirm(trimmedName, qty, imageBytes, selectedCategory?.name)
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
    if (showDuplicateDialog) {
        AlertDialog(
            onDismissRequest = { showDuplicateDialog = false },
            title = { Text("Name already exits in Pantry") },
            text = { Text("An item with this name already exists in your pantry.") },
            confirmButton = {
                TextButton(onClick = { showDuplicateDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}