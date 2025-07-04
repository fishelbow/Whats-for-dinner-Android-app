package com.example.possiblythelastnewproject.features.shoppingList.ui.componets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.possiblythelastnewproject.features.recipe.ui.RecipesViewModel

@Composable
fun CreateShoppingListDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, List<Long>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    val viewModel: RecipesViewModel = hiltViewModel()
    val allRecipes by viewModel.allRecipes.collectAsState()

    val selectedRecipeIds = remember { mutableStateListOf<Long>() }

    val filteredRecipes = remember(searchQuery, allRecipes) {
        if (searchQuery.isBlank()) allRecipes
        else allRecipes.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = {},
        title = { Text("Create Shopping List") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("List Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))
                Text("Search & Select Recipes", style = MaterialTheme.typography.labelLarge)

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Recipes") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                ) {
                    items(filteredRecipes) { recipe ->
                        val isSelected = recipe.id in selectedRecipeIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    if (it) selectedRecipeIds.add(recipe.id)
                                    else selectedRecipeIds.remove(recipe.id)
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(recipe.name)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(name.trim(), selectedRecipeIds.toList())
                },
                enabled = name.isNotBlank() && selectedRecipeIds.isNotEmpty()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewCreateShoppingListDialog() {
    MaterialTheme {
        CreateShoppingListDialog(
            onDismiss = {},
            onConfirm = { _, _ -> }
        )
    }
}