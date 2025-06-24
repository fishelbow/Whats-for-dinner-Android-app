package com.example.possiblythelastnewproject.features.pantry.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.possiblythelastnewproject.core.utils.imagePicker
import com.example.possiblythelastnewproject.features.pantry.data.PantryItem
import com.example.possiblythelastnewproject.features.pantry.domain.InlineBarcodeScanner
import com.example.possiblythelastnewproject.features.pantry.ui.componets.IngredientCard
import com.example.possiblythelastnewproject.features.pantry.ui.componets.IngredientSearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryScreen(
    viewModel: PantryViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    // State from ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val pantryItems by viewModel.pantryItems.collectAsState()
    val inUseIds by viewModel.inUsePantryItemIds.collectAsState(emptySet())

    // Common local variables
    val focusManager = LocalFocusManager.current

    // Local UI state
    var duplicateCodeDetected by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<PantryItem?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newIngredient by remember { mutableStateOf("") }
    var addImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var showScanDialog by remember { mutableStateOf(false) }

    // Create two distinct image pickers by calling your universal function.
    // One is for adding a new ingredient and updates addImageBytes.
    val launchImagePickerForAdd = imagePicker { imageBytes ->
        addImageBytes = imageBytes
    }
    // The other is for editing an existing ingredient.
    // It calls a ViewModel function (or could update local state) for the edit image.
    val launchImagePickerForEdit = imagePicker { imageBytes ->
        viewModel.updateEditImage(imageBytes)
    }

    // Filter ingredients based on search query.
    val filteredItems = pantryItems.filter {
        uiState.searchQuery.isBlank() || it.name.contains(uiState.searchQuery, ignoreCase = true)
    }

    // Main screen scaffold
    Scaffold(
        topBar = {
            IngredientSearchBar(
                searchQuery = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onAddNewIngredient = { showAddDialog = true },
                focusManager = focusManager
            )
        },
        content = { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                if (filteredItems.isEmpty()) {
                    Text(
                        text = "No ingredients found",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredItems) { item ->
                            pantryItems.find { it.id == item.id }?.let { validItem ->
                                IngredientCard(
                                    ingredient = validItem.name,
                                    quantity = validItem.quantity,
                                    imageData = validItem.imageData,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedItem = validItem }
                                )
                            }
                        }
                    }
                }
            }
        }
    )

// --- View Dialog (non-destructive) ---
    selectedItem?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedItem = null },
            title = { Text(item.name) },
            text = {
                Column {
                    IngredientCard(
                        ingredient = item.name,
                        quantity = item.quantity,
                        imageData = item.imageData,
                        category = item.category,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    Text("Category:", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = item.category.ifBlank { "None" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(8.dp))

                    Text("Scan code:", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = item.scanCode ?: "None",
                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { showScanDialog = true }) {
                        Text(
                            if (item.scanCode.isNullOrBlank()) "Link PLU or Barcode"
                            else "Update PLU/Barcode"
                        )
                    }
                    Row {
                        TextButton(onClick = { selectedItem = null }) { Text("Close") }
                        TextButton(onClick = {
                            viewModel.startEditing(item)
                            selectedItem = null
                        }) { Text("Edit") }
                    }
                }
            }
        )
    }

    // --- Add Dialog ---
    if (showAddDialog) {
        val nameExists = pantryItems.any { it.name.equals(newIngredient.trim(), ignoreCase = true) }
        val categories by viewModel.allCategories.collectAsState()
        val selectedCategory = uiState.selectedCategory
        var categoryDropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                newIngredient = ""
                addImageBytes = null
                viewModel.updateSelectedCategory(null)
            },
            title = { Text("Add New Ingredient") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newIngredient,
                        onValueChange = { newIngredient = it },
                        label = { Text("Ingredient") },
                        isError = nameExists,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    IngredientCard(
                        ingredient = newIngredient.ifBlank { "No Name" },
                        quantity = 1,
                        imageData = addImageBytes,
                        category = selectedCategory?.name,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    TextButton(onClick = { launchImagePickerForAdd() }) {
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
                                .menuAnchor()
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
                                        viewModel.updateSelectedCategory(category)
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
                TextButton(onClick = {
                    if (newIngredient.isNotBlank() && !nameExists && selectedCategory != null) {
                        viewModel.addPantryItem(
                            PantryItem(
                                name = newIngredient.trim(),
                                quantity = 1,
                                imageData = addImageBytes,
                                category = selectedCategory.name
                            )
                        )
                        newIngredient = ""
                        addImageBytes = null
                        showAddDialog = false
                        viewModel.updateSelectedCategory(null)
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    newIngredient = ""
                    addImageBytes = null
                    viewModel.updateSelectedCategory(null)
                }) { Text("Cancel") }
            }
        )
    }

    // --- Edit Dialog ---
    uiState.editingItem?.let { item ->
        val categories by viewModel.allCategories.collectAsState()
        var categoryDropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { viewModel.startEditing(null) },
            title = { Text("Edit Ingredient") },
            text = {
                Column {
                    val previewName = uiState.editName.ifBlank { item.name }
                    val previewQty = uiState.editQuantityText.toIntOrNull() ?: item.quantity

                    IngredientCard(
                        ingredient = previewName,
                        quantity = previewQty,
                        imageData = uiState.editImageBytes,
                        category = uiState.editCategory?.name,
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextButton(onClick = { launchImagePickerForEdit() }) {
                        Text("Pick Image")
                    }

                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        OutlinedTextField(
                            value = uiState.editName,
                            onValueChange = {
                                viewModel.updateEditFields(it, uiState.editQuantityText)
                            },
                            label = { Text("Ingredient Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = uiState.editQuantityText,
                            onValueChange = {
                                val digits = it.filter(Char::isDigit)
                                viewModel.updateEditFields(uiState.editName, digits)
                            },
                            label = { Text("Quantity") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )

                        Spacer(Modifier.height(8.dp))
                        ExposedDropdownMenuBox(
                            expanded = categoryDropdownExpanded,
                            onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = uiState.editCategory?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
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
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.confirmEditItem()
                    viewModel.startEditing(null)
                }) { Text("Save Changes") }
            },
            dismissButton = {
                val canDelete = item.id !in inUseIds
                OutlinedButton(
                    onClick = { if (canDelete) viewModel.promptDelete(item) },
                    enabled = canDelete
                ) { Text("Delete…") }

                if (!canDelete) {
                    Text(
                        "Still used in a recipe",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
        )
    }

    // --- Delete Confirmation Dialog ---
    uiState.itemToDelete?.let { item ->
        val isInUse = item.id in inUseIds
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            title = { Text("Delete Ingredient") },
            text = {
                Column {
                    Text("Are you sure you want to delete \"${item.name}\"?")
                    if (isInUse) {
                        Text(
                            "This item is still used in one or more recipes.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) { Text("Cancel") }
            },
            dismissButton = {
                TextButton(
                    onClick = { if (!isInUse) viewModel.confirmDelete() },
                    enabled = !isInUse
                ) { Text("Yes, Delete") }
            }
        )
    }

    // --- Scan Dialog ---
    if (showScanDialog) {
        Dialog(onDismissRequest = { showScanDialog = false }) {
            Surface(shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Scan Barcode")
                    Spacer(Modifier.height(12.dp))
                    InlineBarcodeScanner(
                        onResult = { scannedCode ->
                            val isDuplicate = pantryItems.any {
                                it.scanCode == scannedCode && it.id != selectedItem?.id
                            }
                            if (isDuplicate) {
                                duplicateCodeDetected = true
                                // Do not update scan code for duplicate.
                            } else {
                                selectedItem = selectedItem?.copy(scanCode = scannedCode)
                                selectedItem?.let {
                                    viewModel.updateScanCode(it.id, scannedCode)
                                }
                            }
                            showScanDialog = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f)
                    )
                }
            }
        }
    }

    // --- Duplicate Code Alert Dialog ---
    if (duplicateCodeDetected) {
        AlertDialog(
            onDismissRequest = { duplicateCodeDetected = false },
            confirmButton = {
                TextButton(onClick = { duplicateCodeDetected = false }) { Text("OK") }
            },
            title = { Text("Duplicate Code") },
            text = { Text("That scan code is already linked to another item.") }
        )
    }
}