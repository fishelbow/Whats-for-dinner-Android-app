package com.example.possiblythelastnewproject.features.recipe.ui.componets.ingredientChips

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.recipe.ui.RecipesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIngredientDialog(
    allPantryItems: List<PantryItem>,
    existingIngredientNames: List<String>,
    onAdd: (String, String) -> Unit,
    onDismiss: () -> Unit,
    viewModel: RecipesViewModel // 👈 add this
) {
    var nameQuery by remember { mutableStateOf(viewModel.editUiState.newIngredient) }
    var amount by remember { mutableStateOf("1") }
    var expanded by remember { mutableStateOf(false) }

    var showDuplicateDialog by remember { mutableStateOf(false) }

    val suggestions = remember(nameQuery, allPantryItems) {
        if (nameQuery.isBlank()) emptyList()
        else allPantryItems
            .filter { it.name.contains(nameQuery.trim(), ignoreCase = true) }
            .take(5)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Ingredient") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = nameQuery,
                        onValueChange = {
                            nameQuery = it
                            viewModel.updateNewIngredient(it) // 👈 sync with ViewModel
                            expanded = it.isNotBlank() && suggestions.isNotEmpty()
                        },
                        label = { Text("Ingredient name") },
                        singleLine = true,
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        suggestions.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.name) },
                                onClick = {
                                    nameQuery = item.name
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it.filter { char -> char.isDigit() }
                    },
                    label = { Text("Amount required") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmedName = nameQuery.trim()
                    val isDuplicate = existingIngredientNames.any { it.equals(trimmedName, ignoreCase = true) }

                    if (isDuplicate) {
                        showDuplicateDialog = true
                    } else {
                        onAdd(trimmedName, amount.trim())
                        viewModel.updateNewIngredient("") // ✅ Clear after add
                        onDismiss()
                    }
                },
                enabled = nameQuery.isNotBlank() && amount.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                viewModel.updateNewIngredient("") // ✅ Clear on cancel
                onDismiss()
            }) {
                Text("Cancel")
            }
        }
    )

    if (showDuplicateDialog) {
        AlertDialog(
            onDismissRequest = { showDuplicateDialog = false },

            title = { Text("Duplicate Ingredient") },
            text = { Text("This ingredient has already been added.") },
            confirmButton = {
                TextButton(onClick = { showDuplicateDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}