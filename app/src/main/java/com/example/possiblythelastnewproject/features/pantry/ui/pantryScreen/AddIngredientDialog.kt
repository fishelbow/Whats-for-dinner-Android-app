package com.example.possiblythelastnewproject.features.pantry.ui.pantryScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.possiblythelastnewproject.features.pantry.data.entities.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIngredientDialog(
    newIngredient: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    launchImagePicker: () -> Unit,
    categories: State<List<Category>>,
    selectedCategory: Category?,
    onCategorySelect: (Category?) -> Unit,
    nameExists: Boolean
) {
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var hasSetDefaultCategory by remember(categories.value) { mutableStateOf(false) }

    LaunchedEffect(categories.value) {
        if (!hasSetDefaultCategory && selectedCategory == null && categories.value.isNotEmpty()) {
            val defaultCategory = categories.value.firstOrNull {
                it.name.equals("Other", ignoreCase = true)
            }
            onCategorySelect(defaultCategory)
            hasSetDefaultCategory = true
        }
    }

    AlertDialog(
        onDismissRequest = {},
        title = { Text("Add New Ingredient") },
        text = {
            Column {
                OutlinedTextField(
                    value = newIngredient,
                    onValueChange = onNameChange,
                    label = { Text("Ingredient") },
                    isError = nameExists,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                IngredientCard(
                    ingredient = newIngredient.ifBlank { "No Name" },
                    quantity = 1,
                    modifier = Modifier.padding(top = 8.dp),
                    category = selectedCategory?.name,
                    imageUri = null
                )

                TextButton(onClick = launchImagePicker) {
                    Text("Pick Image")
                }

                Spacer(Modifier.height(8.dp))

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
                        categories.value.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    onCategorySelect(category)
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                if (nameExists) {
                    Text(
                        "This item already exists.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}