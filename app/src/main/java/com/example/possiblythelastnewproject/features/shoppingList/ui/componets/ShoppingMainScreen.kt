package com.example.possiblythelastnewproject.features.shoppingList.ui.componets

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.ShoppingList
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.ListItem

@Composable
fun ShoppingMainScreen(
    shoppingLists: List<ShoppingList>,
    onListClick: (ShoppingList) -> Unit,
    onCreateList: (String, List<Long>, Map<Long, String>) -> Unit,
    onDeleteList: (ShoppingList) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var listToDelete by remember { mutableStateOf<ShoppingList?>(null) }
    val existingListNames = remember(shoppingLists) { shoppingLists.map { it.name } }
    val dateFormatter = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Create New List")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (shoppingLists.isEmpty()) {
                Text(
                    text = "No shopping lists yet.\nTap + to create one.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                    items(shoppingLists) { list ->
                        ListItem(
                            headlineContent = { Text(list.name) },
                            supportingContent = {
                                Text("Created: ${dateFormatter.format(Date(list.createdAt))}")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { onListClick(list) },
                                    onLongClick = { listToDelete = list }
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            if (showCreateDialog) {
                CreateShoppingListDialog(
                    existingListNames = existingListNames,
                    onDismiss = { showCreateDialog = false },
                    onConfirm = { name ->
                        onCreateList(name, emptyList(), emptyMap())
                        showCreateDialog = false
                    }
                )
            }

            listToDelete?.let { list ->
                AlertDialog(
                    onDismissRequest = { listToDelete = null },
                    title = { Text("Delete List") },
                    text = { Text("Delete \"${list.name}\"? This action cannot be undone.") },
                    confirmButton = {
                        TextButton(onClick = {
                            onDeleteList(list)
                            listToDelete = null
                        }) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { listToDelete = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}