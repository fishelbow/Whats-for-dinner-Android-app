package com.example.possiblythelastnewproject.features.shoppingList.ui.componets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.possiblythelastnewproject.features.pantry.ui.PantryViewModel
import com.example.possiblythelastnewproject.features.recipe.ui.RecipesViewModel
import androidx.compose.ui.focus.FocusRequester


@Composable
fun CreateShoppingListDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, List<Long>, Map<Long, String>) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val dummyFocusRequester = remember { FocusRequester() }

    var name by remember { mutableStateOf("") }
    var recipeQuery by remember { mutableStateOf("") }
    var ingredientQuery by remember { mutableStateOf("") }

    val viewModel: RecipesViewModel = hiltViewModel()
    val pantryVM: PantryViewModel = hiltViewModel()
    val allRecipes by viewModel.allRecipes.collectAsState()
    val allIngredients by pantryVM.getAllPantryItems().collectAsState(initial = emptyList())

    val selectedRecipeIds = remember { mutableStateListOf<Long>() }
    val selectedIngredients = remember { mutableStateMapOf<Long, String>() }

    val filteredRecipes = remember(recipeQuery, allRecipes) {
        if (recipeQuery.isBlank()) allRecipes
        else allRecipes.filter { it.name.contains(recipeQuery, ignoreCase = true) }
    }

    val filteredIngredients = remember(ingredientQuery, allIngredients) {
        if (ingredientQuery.isBlank()) allIngredients
        else allIngredients.filter { it.name.contains(ingredientQuery, ignoreCase = true) }
    }

    val isValid = name.isNotBlank() && (
            selectedRecipeIds.isNotEmpty() || selectedIngredients.any { (_, qty) ->
                qty.toDoubleOrNull()?.let { it > 0 } == true
            }
            )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // List Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("List Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                dummyFocusRequester.requestFocus()
                                keyboardController?.hide()
                            }
                        )
                    )

                    // Recipes
                    Column {
                        Text("Search & Select Recipes", style = MaterialTheme.typography.labelLarge)
                        OutlinedTextField(
                            value = recipeQuery,
                            onValueChange = { recipeQuery = it },
                            label = { Text("Search Recipes") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    dummyFocusRequester.requestFocus()
                                    keyboardController?.hide()
                                }
                            )
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

                    // Ingredients
                    Column {
                        Text("Search & Add Ingredients", style = MaterialTheme.typography.labelLarge)
                        OutlinedTextField(
                            value = ingredientQuery,
                            onValueChange = { ingredientQuery = it },
                            label = { Text("Search Ingredients") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    dummyFocusRequester.requestFocus()
                                    keyboardController?.hide()
                                }
                            )
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
                                            onValueChange = { input ->
                                                val digitsOnly = input.filter { it.isDigit() }
                                                val cleaned = digitsOnly.trimStart('0').ifEmpty { "0" }
                                                selectedIngredients[ingredient.id] = cleaned
                                            },
                                            label = { Text("Need") },
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Number,
                                                imeAction = ImeAction.Done
                                            ),
                                            keyboardActions = KeyboardActions(
                                                onDone = {
                                                    dummyFocusRequester.requestFocus()
                                                    keyboardController?.hide()
                                                }
                                            ),
                                            modifier = Modifier.width(80.dp),
                                            singleLine = true
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Spacer(Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                dummyFocusRequester.requestFocus()
                                keyboardController?.hide()
                                onConfirm(
                                    name.trim(),
                                    selectedRecipeIds.toList(),
                                    selectedIngredients
                                )
                            },
                            enabled = isValid
                        ) {
                            Text("Save")
                        }
                    }

                    // Dummy focus target to absorb focus and drop keyboard
                    Box(
                        modifier = Modifier
                            .size(1.dp)
                            .focusRequester(dummyFocusRequester)
                            .focusable()
                    )
                }
            }
        }
    }
}