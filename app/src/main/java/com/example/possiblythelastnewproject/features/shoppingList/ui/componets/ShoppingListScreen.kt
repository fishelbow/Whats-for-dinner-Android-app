package com.example.possiblythelastnewproject.features.shoppingList.ui.componets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.ShoppingListItem
import com.example.possiblythelastnewproject.features.shoppingList.ui.model.CategorizedShoppingItem
import com.example.possiblythelastnewproject.features.shoppingList.ui.model.ShoppingListViewModel
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    navController: NavController,
    listName: String,
    categorizedItems: List<CategorizedShoppingItem>,
    onCheckToggled: (ShoppingListItem) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    var showAddDialog by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }
    var newItemQuantity by remember { mutableStateOf("") }

    val viewModel: ShoppingListViewModel = hiltViewModel()
    val showPantryCreatedDialog by viewModel.showPantryCreatedDialog.collectAsState()
    val pantryItems by viewModel.allPantryItems.collectAsState()
    var hideChecked by remember { mutableStateOf(false) }

    val groupedItems = remember(hideChecked, categorizedItems) {
        categorizedItems
            .filter { !hideChecked || !it.item.isChecked }
            .groupBy { it.category.ifBlank { "Uncategorized" } }
    }

    if (showPantryCreatedDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissPantryCreatedDialog() },
            title = { Text("Pantry Item Created") },
            text = { Text("A new item was added to your pantry.") },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissPantryCreatedDialog() }) {
                    Text("OK")
                }
            }
        )
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                newItemName = ""
                newItemQuantity = ""
            },
            title = { Text("Add Item to List") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    var expanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = newItemName,
                            onValueChange = {
                                newItemName = it
                                expanded = true
                            },
                            label = { Text("Item name") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            singleLine = true
                        )

                        ExposedDropdownMenu(
                            expanded = expanded && newItemName.isNotBlank(),
                            onDismissRequest = { expanded = false }
                        ) {
                            pantryItems
                                .filter { it.name.contains(newItemName, ignoreCase = true) }
                                .take(5)
                                .forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item.name) },
                                        onClick = {
                                            newItemName = item.name
                                            if (newItemQuantity.isBlank() || newItemQuantity == "0") {
                                                newItemQuantity = item.quantity.toString()
                                            }
                                            expanded = false
                                        }
                                    )
                                }
                        }
                    }

                    OutlinedTextField(
                        value = newItemQuantity,
                        onValueChange = { input ->
                            // Keep only digits
                            val digitsOnly = input.filter { it.isDigit() }

                            // Remove leading zeros unless the input is just "0"
                            newItemQuantity = when {
                                digitsOnly.isEmpty() -> ""
                                else -> digitsOnly.trimStart('0').ifEmpty { "0" }
                            }
                        },
                        label = { Text("Quantity (optional)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addItemByName(
                        name = newItemName.trim(),
                        quantity = newItemQuantity.trim()
                    )
                    newItemName = ""
                    newItemQuantity = ""
                    showAddDialog = false
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    newItemName = ""
                    newItemQuantity = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = listName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    FilterChip(
                        selected = hideChecked,
                        onClick = { hideChecked = !hideChecked },
                        label = {
                            Text(if (hideChecked) "Show Checked" else "Hide Checked")
                        }
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                showAddDialog = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Item")
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            groupedItems.forEach { (category, items) ->
                stickyHeader {
                    Surface(
                        tonalElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .padding(vertical = 8.dp, horizontal = 8.dp)
                        )
                    }
                }

                items(items) { categorized ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        ShoppingListRow(
                            item = categorized.item,
                            onCheckToggled = onCheckToggled,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}