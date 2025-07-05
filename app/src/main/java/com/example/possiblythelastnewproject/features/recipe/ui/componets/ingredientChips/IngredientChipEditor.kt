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
import androidx.compose.ui.unit.dp
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
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
    var showDuplicateDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val pantryItemMap = remember(allPantryItems) {
        allPantryItems.associateBy { it.id }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Ingredients",
            style = MaterialTheme.typography.titleMedium
        )

        LazyFlowRow(
            items = ingredients,
            horizontalSpacing = 8.dp,
            verticalSpacing = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) { ingredient ->
            val pantryItem = pantryItemMap[ingredient.pantryItemId]
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