package com.example.possiblythelastnewproject.features.recipe.ui.componets

import androidx.compose.foundation.layout.*
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
    var newIngredient by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            val updatedItem = it.copy(addToShoppingList = !it.addToShoppingList)
                            onToggleShoppingStatus(updatedItem)
                        }
                    },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(ingredient.name)
                            if (isInShoppingList)
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "In Shopping List",
                                    modifier = Modifier.size(16.dp).padding(start = 4.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            if (ingredient.hasScanCode)
                                Icon(
                                    Icons.Default.QrCode2,
                                    contentDescription = "Scannable",
                                    modifier = Modifier.size(16.dp).padding(start = 4.dp)
                                )
                            if (ingredient.pantryItemId == null)
                                Icon(
                                    Icons.Default.NewReleases,
                                    contentDescription = "New",
                                    modifier = Modifier.size(16.dp).padding(start = 4.dp)
                                )
                        }
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            onIngredientsChange(ingredients - ingredient)
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove")
                        }
                    }
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newIngredient,
                onValueChange = { newIngredient = it },
                placeholder = { Text("Add ingredient") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Button(onClick = {
                val trimmed = newIngredient.trim()
                if (trimmed.isNotEmpty()) {
                    coroutineScope.launch {
                        val match = allPantryItems.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
                        val pantryItem = match ?: onRequestCreatePantryItem(trimmed)

                        val newEntry = RecipeIngredientUI(
                            name = pantryItem.name,
                            pantryItemId = pantryItem.id
                        )
                        onIngredientsChange(ingredients + newEntry)
                        newIngredient = ""
                    }
                }
            }) {
                Text("Add")
            }
        }
    }
}

fun <T> List<T>.replace(old: T, new: T): List<T> =
    map { if (it == old) new else it }