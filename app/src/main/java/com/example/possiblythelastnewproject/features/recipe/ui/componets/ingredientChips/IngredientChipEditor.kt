package com.example.possiblythelastnewproject.features.recipe.ui.componets.ingredientChips

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.recipe.ui.RecipesViewModel
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

    val viewModel: RecipesViewModel = hiltViewModel()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Ingredients",
            style = MaterialTheme.typography.titleMedium
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp) // Optional: constrain height
                .verticalScroll(rememberScrollState()) // Optional: allow scrolling
        ) {
            LazyFlowRow(
                items = ingredients,
                horizontalSpacing = 8.dp,
                verticalSpacing = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) { ingredient ->
                val pantryItem = pantryItemMap[ingredient.pantryItemId]
                IngredientChip(
                    ingredient = ingredient,
                    pantryItem = pantryItem,
                    isEditable = true,
                    onToggleShoppingStatus = { updated ->
                        onIngredientsChange(
                            ingredients.map {
                                if (it.pantryItemId == updated.id) {
                                    it.copy(includeInShoppingList = !it.includeInShoppingList)
                                } else it
                            }
                        )
                        onToggleShoppingStatus(updated)
                    },
                    onRemove = {
                        onIngredientsChange(ingredients - ingredient)
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
            existingIngredientNames = ingredients.map { it.name },
            onAdd = { name, amount ->
                scope.launch {
                    val match = allPantryItems.firstOrNull {
                        it.name.equals(name, ignoreCase = true)
                    }
                    val pantryItem = match ?: onRequestCreatePantryItem(name)

                    val alreadyExists = ingredients.any {
                        it.name.equals(pantryItem.name, ignoreCase = true)
                    }

                    if (alreadyExists) {
                        showDuplicateDialog = true
                    } else {
                        onIngredientsChange(
                            ingredients + RecipeIngredientUI(
                                name = pantryItem.name,
                                pantryItemId = pantryItem.id,
                                amountNeeded = amount,
                                includeInShoppingList = true,
                                includeInPantry = true,
                                hasScanCode = pantryItem.scanCode?.isNotBlank() == true
                            )
                        )
                        viewModel.updateNewIngredient("") // ✅ Clear after add
                    }
                }
            },
            onDismiss = {
                showAddDialog = false
                viewModel.updateNewIngredient("") // ✅ Clear on dismiss
            },
            viewModel = viewModel // ✅ Pass it in
        )
    }
}