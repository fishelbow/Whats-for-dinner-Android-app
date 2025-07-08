package com.example.possiblythelastnewproject.features.shoppingList.ui.componets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.possiblythelastnewproject.features.pantry.ui.PantryViewModel
import com.example.possiblythelastnewproject.features.recipe.ui.RecipesViewModel

@Composable
fun CreateShoppingListDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, List<Long>, Map<Long, String>) -> Unit // updated to pass quantities
) {
    var name by remember { mutableStateOf("") }
    var recipeQuery by remember { mutableStateOf("") }
    var ingredientQuery by remember { mutableStateOf("") }

    val viewModel: RecipesViewModel = hiltViewModel()
    val pantryVM: PantryViewModel = hiltViewModel()
    val allRecipes by viewModel.allRecipes.collectAsState()
    val allIngredients by pantryVM.getAllPantryItems().collectAsState(initial = emptyList())

    val selectedRecipeIds = remember { mutableStateListOf<Long>() }
    val selectedIngredients = remember { mutableStateMapOf<Long, String>() } // pantryItemId -> quantityNeeded

    val filteredRecipes = remember(recipeQuery, allRecipes) {
        if (recipeQuery.isBlank()) allRecipes
        else allRecipes.filter { it.name.contains(recipeQuery, ignoreCase = true) }
    }

    val filteredIngredients = remember(ingredientQuery, allIngredients) {
        if (ingredientQuery.isBlank()) allIngredients
        else allIngredients.filter { it.name.contains(ingredientQuery, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = {},
        title = { Text("Create Shopping List") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 📝 List Name
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("List Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 📖 Recipes
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Search & Select Recipes", style = MaterialTheme.typography.labelLarge)
                        OutlinedTextField(
                            value = recipeQuery,
                            onValueChange = { recipeQuery = it },
                            label = { Text("Search Recipes") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(filteredRecipes, key = { it.id }) { recipe ->
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
                }

                // 🧂 Ingredients
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Search & Add Ingredients", style = MaterialTheme.typography.labelLarge)
                        OutlinedTextField(
                            value = ingredientQuery,
                            onValueChange = { ingredientQuery = it },
                            label = { Text("Search Ingredients") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(filteredIngredients, key = { it.id }) { ingredient ->
                                val isSelected = selectedIngredients.containsKey(ingredient.id)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = {
                                            if (it) {
                                                val pantryQty = ingredient.quantity
                                                val defaultQty = if (pantryQty <= 0) "1" else "0"
                                                selectedIngredients[ingredient.id] = defaultQty
                                            } else {
                                                selectedIngredients.remove(ingredient.id)
                                            }
                                        }
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(ingredient.name, modifier = Modifier.weight(1f))
                                    if (isSelected) {
                                        OutlinedTextField(
                                            value = selectedIngredients[ingredient.id] ?: "",
                                            onValueChange = { selectedIngredients[ingredient.id] = it },
                                            label = { Text("Need") },
                                            modifier = Modifier.width(80.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        name.trim(),
                        selectedRecipeIds.toList(),
                        selectedIngredients
                    )
                },
                enabled = name.isNotBlank() && (
                        selectedRecipeIds.isNotEmpty() || selectedIngredients.any { (_, qty) ->
                            qty.toDoubleOrNull()?.let { it > 0 } == true
                        }
                        )
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