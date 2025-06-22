package com.example.possiblythelastnewproject.features.recipe.ui.componets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.possiblythelastnewproject.features.pantry.data.PantryItem
import com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeCreation.RecipeIngredientUI
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientChipEditor(
    ingredients: List<RecipeIngredientUI>,
    onIngredientsChange: (List<RecipeIngredientUI>) -> Unit,
    allPantryItems: List<PantryItem>,
    onRequestCreatePantryItem: suspend (String) -> PantryItem,
    onToggleShoppingStatus: (PantryItem) -> Unit
) {
    val scope      = rememberCoroutineScope()
    var query      by remember { mutableStateOf("") }
    var expanded   by remember { mutableStateOf(false) }

    // filter suggestions
    val suggestions = remember(query, allPantryItems) {
        if (query.isBlank()) emptyList()
        else allPantryItems
            .filter { it.name.contains(query.trim(), ignoreCase = true) }
            .take(5)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Display existing chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(ingredients) { ingredient ->
                val pantryItem = allPantryItems.firstOrNull { it.id == ingredient.pantryItemId }
                val inCart      = pantryItem?.addToShoppingList == true

                InputChip(
                    selected  = inCart,
                    onClick   = {
                        pantryItem?.let {
                            onToggleShoppingStatus(it.copy(addToShoppingList = !it.addToShoppingList))
                        }
                    },
                    label     = {
                        Row {
                            Text(ingredient.name)
                            if (inCart) Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp).padding(start = 4.dp))
                            if (ingredient.hasScanCode) Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(16.dp).padding(start = 4.dp))
                            if (ingredient.pantryItemId == null) Icon(Icons.Default.NewReleases, contentDescription = null, modifier = Modifier.size(16.dp).padding(start = 4.dp))
                        }
                    },
                    trailingIcon = {
                        IconButton(onClick = { onIngredientsChange(ingredients - ingredient) }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove")
                        }
                    }
                )
            }
        }

        // Autocomplete + Add button
        Row(
            modifier            = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded          = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier          = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value            = query,
                    onValueChange    = {
                        query    = it
                        expanded = it.isNotBlank() && suggestions.isNotEmpty()
                    },
                    placeholder      = { Text("Add ingredient") },
                    singleLine       = true,
                    keyboardOptions  = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions  = KeyboardActions(onDone = { expanded = false }),
                    trailingIcon     = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier         = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded        = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    suggestions.forEach { item ->
                        DropdownMenuItem(
                            text    = { Text(item.name) },
                            onClick = {
                                onIngredientsChange(
                                    ingredients + RecipeIngredientUI(item.name, pantryItemId = item.id)
                                )
                                query    = ""
                                expanded = false
                            }
                        )
                    }
                }
            }

            Button(onClick = {
                val trimmed = query.trim()
                if (trimmed.isNotBlank()) {
                    scope.launch {
                        val match = allPantryItems.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
                        val pantryItem = match ?: onRequestCreatePantryItem(trimmed)
                        onIngredientsChange(
                            ingredients + RecipeIngredientUI(pantryItem.name, pantryItemId = pantryItem.id)
                        )
                        query    = ""
                        expanded = false
                    }
                }
            }) {
                Text("Add")
            }
        }
    }
}