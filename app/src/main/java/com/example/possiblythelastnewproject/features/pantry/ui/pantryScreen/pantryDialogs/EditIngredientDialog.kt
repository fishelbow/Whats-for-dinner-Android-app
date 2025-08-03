package com.example.possiblythelastnewproject.features.pantry.ui.pantryScreen.pantryDialogs

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.pantry.ui.PantryViewModel
import com.example.possiblythelastnewproject.features.pantry.ui.pantryScreen.IngredientCard
import com.example.possiblythelastnewproject.features.pantry.ui.pantryScreen.PantryUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditIngredientDialog(
    uiState: PantryUiState,
    pantryItems: List<PantryItem>,
    inUseIds: Set<Long>,
    viewModel: PantryViewModel,
    context: Context,
    launchImagePicker: () -> Unit,
    showDuplicateNameDialog: Boolean,
    onDuplicateNameDialogDismiss: () -> Unit,
    showBlankNameDialog: Boolean,
    onBlankNameDialogDismiss: () -> Unit,
    onShowDuplicateNameDialog: () -> Unit,
    onShowBlankNameDialog: () -> Unit
) {
    val editingItem = uiState.editingItem ?: return
    val categories by viewModel.allCategories.collectAsState()

    LaunchedEffect(categories, editingItem) {
        if (uiState.editCategory == null && categories.isNotEmpty()) {
            val matched = categories.firstOrNull { it.name == editingItem.category }
                ?: categories.firstOrNull { it.name.equals("Other", ignoreCase = true) }
            viewModel.updateEditCategory(matched)
        }
    }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {},
        title = { Text("Edit Ingredient") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                val previewName = uiState.editName.ifBlank { editingItem.name }
                val previewQty = uiState.editQuantityText.toIntOrNull() ?: editingItem.quantity

                IngredientCard(
                    ingredient = previewName,
                    quantity = previewQty,
                    modifier = Modifier.fillMaxWidth(),
                    category = uiState.editCategory?.name,
                    imageUri = uiState.editImageUri
                )

                Spacer(Modifier.height(8.dp))

                TextButton(onClick = launchImagePicker) {
                    Text("Pick Image")
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.editName,
                    onValueChange = { viewModel.updateEditFields(it, uiState.editQuantityText) },
                    label = { Text("Ingredient Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.editQuantityText,
                    onValueChange = {
                        val digits = it.filter(Char::isDigit)
                        viewModel.updateEditFields(uiState.editName, digits)
                    },
                    label = { Text("Quantity") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = uiState.editCategory?.name.orEmpty(),
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
                                    viewModel.updateEditCategory(category)
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val trimmedName = uiState.editName.trim()
                val isDuplicate = pantryItems.any {
                    it.name.equals(trimmedName, ignoreCase = true) && it.id != editingItem.id
                }

                when {
                    trimmedName.isBlank() -> onShowBlankNameDialog()
                    isDuplicate -> onShowDuplicateNameDialog()
                    else -> {
                        viewModel.confirmEditItem(context)
                        viewModel.startEditing(null)
                    }
                }
            }) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            val canDelete = editingItem.id != 0L && editingItem.id !in inUseIds
            Column {
                OutlinedButton(
                    onClick = { if (canDelete) viewModel.promptDelete(editingItem) },
                    enabled = canDelete
                ) {
                    Text("Delete…")
                }

                if (!canDelete) {
                    Text(
                        "Still used in a recipe",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
        }
    )

    if (showDuplicateNameDialog) {
        AlertDialog(
            onDismissRequest = onDuplicateNameDialogDismiss,
            title = { Text("Duplicate Name") },
            text = { Text("An ingredient with this name already exists. Please choose a different name.") },
            confirmButton = {
                TextButton(onClick = onDuplicateNameDialogDismiss) { Text("OK") }
            }
        )
    }

    if (showBlankNameDialog) {
        AlertDialog(
            onDismissRequest = onBlankNameDialogDismiss,
            title = { Text("Missing Ingredient Name") },
            text = { Text("Please enter a name for the ingredient before saving.") },
            confirmButton = {
                TextButton(onClick = onBlankNameDialogDismiss) { Text("OK") }
            }
        )
    }
}