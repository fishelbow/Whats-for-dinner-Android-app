package com.example.possiblythelastnewproject.features.recipe.ui.componets.ingredientChips

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    var showNewPantryItemDialog by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val pantryItemMap = remember(allPantryItems) {
        allPantryItems.associateBy { it.id }.toMutableMap()
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Ingredients", style = MaterialTheme.typography.titleMedium)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp)
                .verticalScroll(rememberScrollState())
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
                                if (it.pantryItemId == updated.id)
                                    it.copy(includeInShoppingList = !it.includeInShoppingList)
                                else it
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

        showNewPantryItemDialog?.let { name ->
            AlertDialog(
                onDismissRequest = { showNewPantryItemDialog = null },
                title = { Text("Pantry Item Created") },
                text = { Text("“$name” was added to your pantry with quantity 0.") },
                confirmButton = {
                    TextButton(onClick = { showNewPantryItemDialog = null }) {
                        Text("OK")
                    }
                }
            )
        }

        if (showAddDialog) {
            AddIngredientDialog(
                allPantryItems = allPantryItems,
                existingIngredientNames = ingredients.map { it.name },
                onAdd = { name, amount ->
                    scope.launch {
                        val trimmedName = name.trim()

                        val match = pantryItemMap.values.firstOrNull {
                            it.name.equals(trimmedName, ignoreCase = true)
                        }

                        val pantryItem = match ?: onRequestCreatePantryItem(trimmedName).also {
                            showNewPantryItemDialog = it.name
                            pantryItemMap[it.id] = it // ✅ Inject into local map
                        }

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
                        }

                        showAddDialog = false
                    }
                },
                onDismiss = { showAddDialog = false }
            )
        }
    }
}