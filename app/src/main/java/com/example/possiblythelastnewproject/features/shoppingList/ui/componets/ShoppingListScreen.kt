package com.example.possiblythelastnewproject.features.shoppingList.ui.componets

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.possiblythelastnewproject.features.shoppingList.ui.model.ShoppingListViewModel
import com.example.possiblythelastnewproject.features.recipe.ui.RecipesViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.RecipeSelection
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.ShoppingListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel,
    recipesViewModel: RecipesViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val allLists by viewModel.allShoppingLists.collectAsState()
    val activeId by viewModel.activeListId.collectAsState()
    val hideFound by viewModel.hideFound.collectAsState()
    val selectedRecipes by viewModel.recipeSelections.collectAsState()
    val visibleItems by viewModel.visibleItems.collectAsState()
    val showCreateDialog by viewModel.showCreatePantryItemDialog.collectAsState()
    val allRecipes by recipesViewModel.allRecipes.collectAsState(emptyList())
    val pantryItems by viewModel.pantryItems.collectAsState()

    var showPickerDialog by remember { mutableStateOf(false) }
    var showRecipeOverlay by remember { mutableStateOf(false) }
    var showIngredientOverlay by remember { mutableStateOf(false) }
    var recipeQuery by remember { mutableStateOf("") }
    var ingredientQuery by remember { mutableStateOf("") }
    var recipesExpanded by remember { mutableStateOf(true) }

    // dialog ui state
    var recipeToRemove by remember { mutableStateOf<RecipeSelection?>(null) }
    var itemToDelete by remember { mutableStateOf<ShoppingListItem?>(null) }


    // Ingredient deletion dialog
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Ingredient?") },
            text = { Text("“${item.name}” will be removed from your shopping list.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteItem(item)
                    itemToDelete = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }


    // Recipe deletion dialog
    recipeToRemove?.let { sel ->
        val name = allRecipes.firstOrNull { it.id == sel.recipeId }?.name ?: "Recipe #${sel.recipeId}"
        val ingredients = viewModel.getGeneratedIngredientsForRecipe(sel.recipeId)

        AlertDialog(
            onDismissRequest = { recipeToRemove = null },
            title = { Text("Remove Recipe?") },
            text = {
                Column {
                    Text("“$name” will be removed. This will also delete:")
                    Spacer(Modifier.height(8.dp))
                    ingredients.forEach {
                        Text("• ${it.name} (${it.quantity})")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeRecipe(sel.recipeId)
                    recipeToRemove = null
                }) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { recipeToRemove = null }) {
                    Text("Cancel")
                }
            }
        )
    }




    val listName = remember(allLists, activeId) {
        allLists.firstOrNull { it.id == activeId }?.name ?: "Shopping List"
    }

    val grouped = remember(visibleItems) {
        visibleItems.groupBy { it.category.ifBlank { "Uncategorized" } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(listName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.undoLast() }) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                    }
                    IconButton(onClick = { viewModel.setHideFound(!hideFound) }) {
                        Icon(
                            if (hideFound) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = "Toggle Hide Found"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showPickerDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
// 📂 Selected Recipes
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                        .combinedClickable { recipesExpanded = !recipesExpanded }
                        .animateContentSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Selected Recipes (×${selectedRecipes.sumOf { it.count }})",
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Icon(
                        imageVector = if (recipesExpanded) Icons.Filled.KeyboardArrowUp
                        else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            if (recipesExpanded) {
                items(selectedRecipes) { sel ->
                    val name = allRecipes.firstOrNull { it.id == sel.recipeId }?.name
                        ?: "Recipe #${sel.recipeId}"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .combinedClickable(
                                onClick = { /* TODO: view/edit */ },
                                onLongClick = { recipeToRemove = sel }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(name, Modifier.weight(1f))
                        Badge { Text("×${sel.count}") }
                    }
                }
                item { HorizontalDivider() }
            }

// 🧺 Grouped Items
            grouped.forEach { (category, items) ->
                item {
                    Text(
                        category,
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(12.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(items) { catItem ->
                    val item = catItem.item
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .combinedClickable(
                                onClick = {},
                                onLongClick = { itemToDelete = item }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(item.name, style = MaterialTheme.typography.bodyLarge)
                            Text(item.quantity, style = MaterialTheme.typography.bodyMedium)
                        }
                        Checkbox(
                            checked = item.isChecked,
                            onCheckedChange = { viewModel.toggleItemChecked(item.uuid, it) }
                        )
                    }
                }
            }
        }
    }

// 🎛️ Picker Dialog
    if (showPickerDialog) {
        AlertDialog(
            onDismissRequest = { showPickerDialog = false },
            title = { Text("Add to Shopping List") },
            text = { Text("What would you like to add?") },
            confirmButton = {
                Column {
                    TextButton(onClick = {
                        showPickerDialog = false
                        showRecipeOverlay = true
                    }) { Text("Add Recipe") }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = {
                        showPickerDialog = false
                        showIngredientOverlay = true
                    }) { Text("Add Ingredient") }
                }
            },
            dismissButton = {
                TextButton(onClick = { showPickerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

// 🍽️ Fullscreen Overlay: Recipe Search
    if (showRecipeOverlay) {
        val matches = allRecipes
            .map { it.name }
            .filter { it.contains(recipeQuery.trim(), ignoreCase = true) }

        AddOverlay(
            title = "Add Recipe",
            query = recipeQuery,
            onQueryChange = { recipeQuery = it },
            results = matches,
            onSelect = { name ->
                allRecipes.firstOrNull { it.name == name }?.let {
                    viewModel.addRecipe(it.id)
                }
                showRecipeOverlay = false
                recipeQuery = ""
            },
            onDismiss = { showRecipeOverlay = false }
        )
    }

// 🧂 Fullscreen Overlay: Ingredient Search
    if (showIngredientOverlay) {
        val matches = pantryItems
            .map { it.name }
            .filter { it.contains(ingredientQuery.trim(), ignoreCase = true) }

        AddOverlay(
            title = "Add Ingredient",
            query = ingredientQuery,
            onQueryChange = { ingredientQuery = it },
            results = matches.ifEmpty { listOf("Add '${ingredientQuery}' as new item") },
            onSelect = { name ->
                viewModel.addIngredientByName(name.trim(), "1")
                showIngredientOverlay = false
                ingredientQuery = ""
            },
            onDismiss = { showIngredientOverlay = false }
        )
    }

// 🧾 Confirm Pantry Item Dialog
    showCreateDialog?.let { temp ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelCreatePantryItem() },
            title = { Text("Create New Pantry Item?") },
            text = { Text("“${temp.name}” will be added with zero quantity and flagged for shopping. Continue?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmCreatePantryItem(temp) }) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelCreatePantryItem() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AddOverlay(
    title: String,
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text("Search by name") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
            )
            Spacer(Modifier.height(12.dp))
            LazyColumn {
                items(results) { name ->
                    Text(
                        text = name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onSelect(name) },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                Text("Close")
            }
        }
    }
}