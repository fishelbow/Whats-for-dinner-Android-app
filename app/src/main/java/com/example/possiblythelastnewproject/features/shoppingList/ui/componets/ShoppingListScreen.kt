package com.example.possiblythelastnewproject.features.shoppingList.ui.componets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.ShoppingListItem
import com.example.possiblythelastnewproject.features.shoppingList.ui.model.CategorizedShoppingItem
import com.example.possiblythelastnewproject.features.shoppingList.ui.model.ShoppingListViewModel
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.example.possiblythelastnewproject.features.recipe.ui.RecipesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
navController: NavController,
listName: String,
categorizedItems: List<CategorizedShoppingItem>,
onCheckToggled: (ShoppingListItem) -> Unit
) {
val keyboardController = LocalSoftwareKeyboardController.current
val viewModel: ShoppingListViewModel = hiltViewModel()

val showPantryCreatedDialog by viewModel.showPantryCreatedDialog.collectAsState()
val pantryItems by viewModel.allPantryItems.collectAsState()

var showSelectorDialog by remember { mutableStateOf(false) }
var showAddDialog by remember { mutableStateOf(false) }
var newItemName by remember { mutableStateOf("") }
var newItemQuantity by remember { mutableStateOf("") }
var hideChecked by remember { mutableStateOf(false) }
var showRecipeDialog by remember { mutableStateOf(false) }
var recipeQuery by remember { mutableStateOf("") }
var selectedItemForEdit by remember { mutableStateOf<ShoppingListItem?>(null) }
var updatedQuantity by remember { mutableStateOf("") }
val recipesViewModel: RecipesViewModel = hiltViewModel()
val allRecipes by recipesViewModel.allRecipes.collectAsState()

val selectedRecipeCounts = remember { mutableStateMapOf<Long, Int>() }
val snackbarHostState = remember { SnackbarHostState() }

val groupedItems = remember(hideChecked, categorizedItems) {
categorizedItems
.filter { !hideChecked || !it.item.isChecked }
.groupBy { it.category.ifBlank { "Uncategorized" } }
}

if (showRecipeDialog) {
AlertDialog(
onDismissRequest = { showRecipeDialog = false },
title = { Text("Select Recipes") },
text = {
Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    OutlinedTextField(
        value = recipeQuery,
        onValueChange = { recipeQuery = it },
        label = { Text("Search Recipes") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
    )

    val filteredRecipes = allRecipes.filter {
        it.name.contains(recipeQuery, ignoreCase = true)
    }

    LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
        items(filteredRecipes, key = { it.id }) { recipe ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = selectedRecipeCounts.contains(recipe.id),
                    onCheckedChange = { isChecked ->
                        if (isChecked) selectedRecipeCounts[recipe.id] = 1
                        else selectedRecipeCounts.remove(recipe.id)
                    }
                )
                Spacer(Modifier.width(8.dp))
                Text(recipe.name, modifier = Modifier.weight(1f))
                if (selectedRecipeCounts.contains(recipe.id)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            val current = selectedRecipeCounts[recipe.id] ?: 1
                            if (current > 1) selectedRecipeCounts[recipe.id] =
                                current - 1
                        }) { Text("-") }
                        Text("${selectedRecipeCounts[recipe.id]}")
                        IconButton(onClick = {
                            val current = selectedRecipeCounts[recipe.id] ?: 1
                            selectedRecipeCounts[recipe.id] = current + 1
                        }) { Text("+") }
                    }
                }
            }
        }
    }
}
},
confirmButton = {
TextButton(onClick = {
    showRecipeDialog = false
    viewModel.mergeSelectedRecipesIntoActiveList(selectedRecipeCounts.toMap())

    val snackMessage =
        selectedRecipeCounts.entries.joinToString(", ") { (id, count) ->
            val name = allRecipes.find { it.id == id }?.name ?: "Recipe"
            "$name ×$count"
        }

    CoroutineScope(Dispatchers.Main).launch {
        snackbarHostState.showSnackbar("Added: $snackMessage")
    }

    selectedRecipeCounts.clear()
    recipeQuery = ""
}) {
    Text("Add to List")
}
},
dismissButton = {
TextButton(onClick = {
    showRecipeDialog = false
    recipeQuery = ""
    selectedRecipeCounts.clear()
}) {
    Text("Cancel")
}
}
)
}

if (showPantryCreatedDialog) {
AlertDialog(
onDismissRequest = { viewModel.dismissPantryCreatedDialog() },
title = { Text("Pantry Item Created") },
text = { Text("A new item was added to your pantry.") },
confirmButton = {
TextButton(onClick = { viewModel.dismissPantryCreatedDialog() }) {
    Text("OK")
}
}
)
}

if (showSelectorDialog) {
AlertDialog(
onDismissRequest = { showSelectorDialog = false },
title = { Text("Add New...") },
text = { Text("Would you like to add an ingredient or a recipe?") },
confirmButton = {
TextButton(onClick = {
    showSelectorDialog = false
    showAddDialog = true
}) { Text("Ingredient") }
},
dismissButton = {
TextButton(onClick = {
    showSelectorDialog = false
    showRecipeDialog = true
}) { Text("Recipe") }
}
)
}

if (showAddDialog) {
AlertDialog(
onDismissRequest = {
showAddDialog = false
newItemName = ""
newItemQuantity = ""
},
title = { Text("Add Item to List") },
text = {
Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = newItemName,
            onValueChange = {
                newItemName = it
                expanded = true
            },
            label = { Text("Item name") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            singleLine = true
        )

        ExposedDropdownMenu(
            expanded = expanded && newItemName.isNotBlank(),
            onDismissRequest = { expanded = false }
        ) {
            pantryItems
                .filter { it.name.contains(newItemName, ignoreCase = true) }
                .take(5)
                .forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.name) },
                        onClick = {
                            newItemName = item.name
                            if (newItemQuantity.isBlank() || newItemQuantity == "0") {
                                newItemQuantity = item.quantity.toString()
                            }
                            expanded = false
                        }
                    )
                }
        }
    }

    OutlinedTextField(
        value = newItemQuantity,
        onValueChange = { input ->
            val digitsOnly = input.filter { it.isDigit() }
            val cleaned = digitsOnly.trimStart('0').ifEmpty { "1" }
            val clamped = if ((cleaned.toIntOrNull() ?: 0) < 1) "1" else cleaned
            newItemQuantity = clamped
        },
        label = { Text("Quantity (min 1)") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { keyboardController?.hide() }
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
},
confirmButton = {
TextButton(onClick = {
    val finalQuantity = newItemQuantity.trim().ifBlank { "1" }
    viewModel.addItemByName(
        name = newItemName.trim(),
        quantity = finalQuantity
    )
    newItemName = ""
    newItemQuantity = ""
    showAddDialog = false
}) {
    Text("Add")
}
},
dismissButton = {
TextButton(onClick = {
    showAddDialog = false
    newItemName = ""
    newItemQuantity = ""
}) {
    Text("Cancel")
}
}
)
}

Scaffold(
topBar = {
TopAppBar(
title = { Text(text = listName) },
navigationIcon = {
    IconButton(onClick = { navController.popBackStack() }) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
    }
},
actions = {
    FilterChip(
        selected = hideChecked,
        onClick = { hideChecked = !hideChecked },
        label = {
            Text(if (hideChecked) "Show Checked" else "Hide Checked")
        }
    )
}
)
},
floatingActionButton = {
FloatingActionButton(onClick = {
showSelectorDialog = true
}) {
Icon(Icons.Filled.Add, contentDescription = "Add")
}
},
snackbarHost = { SnackbarHost(snackbarHostState) }
) { padding ->
LazyColumn(
contentPadding = padding,
verticalArrangement = Arrangement.spacedBy(8.dp),
modifier = Modifier.padding(horizontal = 16.dp)
) {
groupedItems.forEach { (category, items) ->
stickyHeader {
    Surface(
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
        )
    }
}

items(items) { categorized ->
    ShoppingListCard(
        item = categorized.item,
        onCheckToggled = onCheckToggled,
        onLongPress = { item ->
            selectedItemForEdit = item
            updatedQuantity =
                item.quantity.toDoubleOrNull()?.toInt()?.toString() ?: item.quantity
        }
    )
}
}
}
}
}


