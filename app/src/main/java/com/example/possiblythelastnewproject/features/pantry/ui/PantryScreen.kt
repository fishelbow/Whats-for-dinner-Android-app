package com.example.possiblythelastnewproject.features.pantry.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.possiblythelastnewproject.core.utils.compressImageFromUri
import com.example.possiblythelastnewproject.features.pantry.data.PantryItem
import com.example.possiblythelastnewproject.features.pantry.ui.componets.IngredientCard
import com.example.possiblythelastnewproject.features.pantry.ui.componets.IngredientSearchBar
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.window.Dialog
import com.example.possiblythelastnewproject.features.pantry.domain.InlineBarcodeScanner
import com.example.possiblythelastnewproject.features.pantry.ui.componets.BarcodeScanDialog


@Composable
fun PantryScreen(
viewModel: PantryViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pantryItems by viewModel.pantryItems.collectAsState()
    val inUseIds by viewModel.inUsePantryItemIds.collectAsState(emptySet())

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var duplicateCodeDetected by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<PantryItem?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newIngredient by remember { mutableStateOf("") }
    var addImageBytes by remember { mutableStateOf<ByteArray?>(null) }

    var showScanDialog by remember { mutableStateOf(false) }

    val filteredItems = pantryItems.filter {
        uiState.searchQuery.isBlank() || it.name.contains(uiState.searchQuery, ignoreCase = true)
    }

    val addImageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                coroutineScope.launch {
                    addImageBytes = compressImageFromUri(context, it, 300, 300, 80)
                }
            }
        }

    val viewImageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                coroutineScope.launch {
                    val bytes = compressImageFromUri(context, it, 300, 300, 80)
                    selectedItem = selectedItem?.copy(imageData = bytes)
                }
            }
        }

    val editImageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                coroutineScope.launch {
                    viewModel.updateEditImage(compressImageFromUri(context, it, 300, 300, 80))
                }
            }
        }

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
                        "No ingredients found",
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

// View dialog (non-destructive)
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
                        modifier = Modifier.fillMaxWidth()
                    )
                    //TODO here is what i need for the white board
                    Spacer(Modifier.height(8.dp))


                    TextButton(onClick = { showScanDialog = true }) {
                        Text(if (item.scanCode.isNullOrBlank()) "Link PLU or Barcode" else "Update PLU/Barcode")
                    }
                    // I need a text here that displays the scanCode from the PantryItem
                    Text(
                        text = "Scan code:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = item.scanCode ?: "None",
                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.primary
                    )


                }
            },
            confirmButton = {
                Row {
                    TextButton(onClick = { selectedItem = null }) {
                        Text("Close")
                    }
                    TextButton(onClick = {
                        viewModel.startEditing(selectedItem)
                        selectedItem = null
                    }) {
                        Text("Edit")
                    }
                }
            }
        )
    }

// Add dialog
    if (showAddDialog) {
        val nameExists = pantryItems.any { it.name.equals(newIngredient.trim(), ignoreCase = true) }
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                newIngredient = ""
                addImageBytes = null
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
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    TextButton(onClick = { addImageLauncher.launch("image/*") }) {
                        Text("Pick Image")
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
                    if (newIngredient.isNotBlank() && !nameExists) {
                        viewModel.addPantryItem(
                            PantryItem(
                                name = newIngredient.trim(),
                                quantity = 1,
                                imageData = addImageBytes
                            )
                        )
                        newIngredient = ""
                        addImageBytes = null
                        showAddDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    newIngredient = ""
                    addImageBytes = null
                }) { Text("Cancel") }
            }
        )
    }

// Edit dialog (unchanged logic)
    uiState.editingItem?.let { item ->
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
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(onClick = { editImageLauncher.launch("image/*") }) {
                        Text("Pick Image")
                    }
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        OutlinedTextField(
                            value = uiState.editName,
                            onValueChange = {
                                viewModel.updateEditFields(
                                    it,
                                    uiState.editQuantityText
                                )
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

// Delete confirmation
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
                TextButton(onClick = { viewModel.cancelDelete() }) {
                    Text("Cancel")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!isInUse) viewModel.confirmDelete()
                    },
                    enabled = !isInUse
                ) {
                    Text("Yes, Delete")
                }
            }
        )
    }

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
                                // Don't update selectedItem.scanCode
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

    if (duplicateCodeDetected) {
        AlertDialog(
            onDismissRequest = { duplicateCodeDetected = false },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = { duplicateCodeDetected = false }
                ) { Text("OK") }
            },
            title = { Text("Duplicate Code") },
            text = { Text("That scan code is already linked to another item.") }
        )
    }
}
