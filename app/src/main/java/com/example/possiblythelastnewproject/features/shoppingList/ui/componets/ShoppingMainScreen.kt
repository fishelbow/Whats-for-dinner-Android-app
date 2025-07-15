package com.example.possiblythelastnewproject.features.shoppingList.ui.componets

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.ShoppingList
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingMainScreen(
    shoppingLists: List<ShoppingList>,
    onListClick: (ShoppingList) -> Unit,
    onCreateList: (String) -> Unit,
    onDeleteList: (ShoppingList) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<ShoppingList?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Validate as user types in dialog
    LaunchedEffect(newName, shoppingLists) {
        nameError = when {
            newName.isBlank() -> "Name cannot be empty"
            shoppingLists.any { it.name.equals(newName.trim(), ignoreCase = true) } ->
                "You already have a list named “${newName.trim()}”"
            else -> null
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Your Shopping Lists") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                newName = ""
                nameError = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Create List")
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            LazyColumn {
                items(shoppingLists) { list ->
                    val formattedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        .format(list.createdAt)

                    ListItem(
                        headlineContent = { Text(list.name) },
                        supportingContent = { Text("Created: $formattedDate") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onListClick(list) },
                                onLongClick = { pendingDelete = list }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    HorizontalDivider()
                }
            }

            // Create new-list dialog
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("New Shopping List") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = newName,
                                onValueChange = { newName = it },
                                label = { Text("List Name") },
                                isError = nameError != null,
                                singleLine = true
                            )
                            nameError?.let { err ->
                                Text(
                                    text = err,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val trimmed = newName.trim()
                                if (nameError == null) {
                                    onCreateList(trimmed)
                                    keyboardController?.hide()
                                    showDialog = false
                                }
                            }
                        ) { Text("Create") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Delete confirmation dialog
            pendingDelete?.let { target ->
                AlertDialog(
                    onDismissRequest = { pendingDelete = null },
                    title = { Text("Delete list?") },
                    text = { Text("Are you sure you want to delete “${target.name}”?") },
                    confirmButton = {
                        TextButton(onClick = {
                            onDeleteList(target)
                            pendingDelete = null
                        }) { Text("Delete") }
                    },
                    dismissButton = {
                        TextButton(onClick = { pendingDelete = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}