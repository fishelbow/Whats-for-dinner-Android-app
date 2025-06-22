package com.example.possiblythelastnewproject.features.recipe.ui.componets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.possiblythelastnewproject.features.pantry.data.PantryItem
import com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeCreation.RecipeIngredientUI
import kotlinx.coroutines.launch

@Composable
fun IngredientChipEditor(
    ingredients: List<RecipeIngredientUI>,
    onIngredientsChange: (List<RecipeIngredientUI>) -> Unit,
    allPantryItems: List<PantryItem>,
    onRequestCreatePantryItem: suspend (String) -> PantryItem,
    onToggleShoppingStatus: (PantryItem) -> Unit
) {
    // 1) States
    var newIngredient by remember { mutableStateOf("") }
    var suggestionExpanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // 2) Prepare suggestion list
    val suggestions = remember(newIngredient, allPantryItems) {
        if (newIngredient.isBlank()) emptyList()
        else allPantryItems
            .filter {
                it.name.contains(newIngredient.trim(), ignoreCase = true)
            }
            .take(5)
    }

    // 3) bringIntoView helper
    val bringRequester = remember { BringIntoViewRequester() }
    LaunchedEffect(suggestionExpanded) {
        if (suggestionExpanded) bringRequester.bringIntoView()
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // --- existing chips ---
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ingredients.forEach { ingredient ->
                val pantryItem = allPantryItems.firstOrNull { it.id == ingredient.pantryItemId }
                val isInShoppingList = pantryItem?.addToShoppingList == true

                InputChip(
                    selected = isInShoppingList,
                    onClick = {
                        pantryItem?.let {
                            onToggleShoppingStatus(it.copy(addToShoppingList = !it.addToShoppingList))
                        }
                    },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(ingredient.name)
                            if (isInShoppingList) {
                                Icon(
                                    Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp).padding(start = 4.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (ingredient.hasScanCode) {
                                Icon(
                                    Icons.Default.QrCode2,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp).padding(start = 4.dp)
                                )
                            }
                            if (ingredient.pantryItemId == null) {
                                Icon(
                                    Icons.Default.NewReleases,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp).padding(start = 4.dp)
                                )
                            }
                        }
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            onIngredientsChange(ingredients - ingredient)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove ingredient"
                            )
                        }
                    }
                )
            }
        }

        // --- input + suggestions ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .bringIntoViewRequester(bringRequester)
            ) {
                OutlinedTextField(
                    value = newIngredient,
                    onValueChange = {
                        newIngredient = it
                        suggestionExpanded = it.isNotBlank()
                    },
                    placeholder = { Text("Add ingredient") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                AnimatedVisibility(visible = suggestionExpanded && suggestions.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(vertical = 4.dp)
                    ) {
                        items(suggestions) { item ->
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // pick an existing pantry item
                                        onIngredientsChange(
                                            ingredients + RecipeIngredientUI(
                                                name = item.name,
                                                pantryItemId = item.id
                                            )
                                        )
                                        newIngredient = ""
                                        suggestionExpanded = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            Button(onClick = {
                val trimmed = newIngredient.trim()
                if (trimmed.isNotEmpty()) {
                    coroutineScope.launch {
                        // exact match? else create
                        val match = allPantryItems.firstOrNull {
                            it.name.equals(trimmed, ignoreCase = true)
                        }
                        val pantryItem = match ?: onRequestCreatePantryItem(trimmed)
                        onIngredientsChange(
                            ingredients + RecipeIngredientUI(
                                name = pantryItem.name,
                                pantryItemId = pantryItem.id
                            )
                        )
                        newIngredient = ""
                        suggestionExpanded = false
                    }
                }
            }) {
                Text("Add")
            }
        }
    }
}