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
import com.example.possiblythelastnewproject.features.shoppingList.data.entity.ShoppingList
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.ListItem
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ShoppingMainScreen(
    shoppingLists: List<ShoppingList>,
    onListClick: (ShoppingList) -> Unit,
    onCreateList: (String, List<Long>) -> Unit,
    onDeleteList: (ShoppingList) -> Unit // NEW: delete callback
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var listToDelete by remember { mutableStateOf<ShoppingList?>(null) }
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
            when {
                shoppingLists.isEmpty() -> {
                    Text(
                        text = "No shopping lists yet.\nTap + to create one.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
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
            }

            if (showCreateDialog) {
                CreateShoppingListDialog(
                    onDismiss = { showCreateDialog = false },
                    onConfirm = { name, selectedRecipeIds ->
                        onCreateList(name, selectedRecipeIds)
                        showCreateDialog = false
                    }
                )
            }

            if (listToDelete != null) {
                AlertDialog(
                    onDismissRequest = { listToDelete = null },
                    title = { Text("Delete List") },
                    text = { Text("Delete \"${listToDelete!!.name}\"? This action cannot be undone.") },
                    confirmButton = {
                        TextButton(onClick = {
                            onDeleteList(listToDelete!!)
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


@Preview(showBackground = true)
@Composable
fun ShoppingMainScreenPreview() {
    val mockLists = listOf(
        ShoppingList(id = 1L, name = "Weekend Groceries", createdAt = System.currentTimeMillis()),
        ShoppingList(id = 2L, name = "Camping Trip", createdAt = System.currentTimeMillis() - 86400000L),
        ShoppingList(id = 3L, name = "Dinner Party", createdAt = System.currentTimeMillis() - 172800000L)
    )

    MaterialTheme {
        ShoppingMainScreen(
            shoppingLists = mockLists,
            onListClick = {},
            onCreateList = { _, _ -> },
            onDeleteList = {} // Added dummy handler for preview
        )
    }
}