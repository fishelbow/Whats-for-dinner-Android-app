package com.example.possiblythelastnewproject.features.recipe.ui.componets.ingredientChips

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.recipe.ui.components.ingredientChips.AddIngredientDialog
import com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeCreation.RecipeIngredientUI
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.FlowRow

@Composable
fun IngredientChipEditor(
    ingredients: List<RecipeIngredientUI>,
    onIngredientsChange: (List<RecipeIngredientUI>) -> Unit,
    allPantryItems: List<PantryItem>,
    onRequestCreatePantryItem: suspend (String) -> PantryItem,
    onToggleShoppingStatus: (PantryItem) -> Unit
) {
    var showDuplicateDialog by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val suggestions = remember(query, allPantryItems) {
        if (query.isBlank()) emptyList()
        else allPantryItems
            .filter { it.name.contains(query.trim(), ignoreCase = true) }
            .take(5)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Ingredients",
            style = MaterialTheme.typography.titleMedium
        )


        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ingredients.forEach { ingredient ->
                val pantryItem = allPantryItems.firstOrNull { it.id == ingredient.pantryItemId }
                val inCart = pantryItem?.addToShoppingList == true

                InputChip(
                    selected = inCart,
                    onClick = {
                        pantryItem?.let {
                            onToggleShoppingStatus(it.copy(addToShoppingList = !it.addToShoppingList))
                        }
                    },
                    label = {
                        Row {
                            Text("${ingredient.amountRequired} × ${pantryItem?.name ?: ingredient.name}")
                            if (inCart) {
                                Icon(
                                    Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp).padding(start = 4.dp)
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
                        IconButton(onClick = { onIngredientsChange(ingredients - ingredient) }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove")
                        }
                    }
                )
            }
        }
        Button(onClick = { showAddDialog = true }) {
            Text("Add")
        }

        if (showDuplicateDialog) {
            AlertDialog(
                onDismissRequest = { showDuplicateDialog = false },
                confirmButton = {
                    TextButton(onClick = { showDuplicateDialog = false }) {
                        Text("OK")
                    }
                },
                title = { Text("Duplicate Ingredient") },
                text = { Text("You’ve already added that item.") }
            )
        }
    }

    if (showAddDialog) {
        AddIngredientDialog(
            allPantryItems = allPantryItems,
            onAdd = { name, amount ->
                scope.launch {
                    val match = allPantryItems.firstOrNull {
                        it.name.equals(name, ignoreCase = true)
                    }
                    val pantryItem = match ?: onRequestCreatePantryItem(name)
                    onIngredientsChange(
                        ingredients + RecipeIngredientUI(
                            name = pantryItem.name,
                            pantryItemId = pantryItem.id,
                            amountNeeded = amount
                        )
                    )
                }
            },
            onDismiss = { showAddDialog = false }
        )
    }

}