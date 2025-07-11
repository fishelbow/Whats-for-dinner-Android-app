package com.example.possiblythelastnewproject.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.pantry.ui.PantryScreen
import com.example.possiblythelastnewproject.features.pantry.ui.PantryViewModel
import com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeDetail.RecipeDetailScreen
import com.example.possiblythelastnewproject.features.recipe.ui.componets.mainScreen.RecipeScreenWithSearch
import com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeCreation.RecipeCreationFormScreen
import com.example.possiblythelastnewproject.features.scan.ui.ScanViewModel
import com.example.possiblythelastnewproject.features.scan.ui.ScanningTab
import com.example.possiblythelastnewproject.features.scan.ui.componets.CreateFromScanDialog
import com.example.possiblythelastnewproject.features.scan.ui.componets.LinkScanCodeDialog
import com.example.possiblythelastnewproject.features.scan.ui.componets.UpdateItemDialog
import com.example.possiblythelastnewproject.features.shoppingList.ui.componets.ShoppingListScreen
import com.example.possiblythelastnewproject.features.shoppingList.ui.model.ShoppingListViewModel
import com.example.possiblythelastnewproject.features.shoppingList.ui.componets.ShoppingMainScreen

// 1) Tab model
sealed class TabItem(val title: String, val icon: ImageVector) {
    data object Recipes : TabItem("Recipes", Icons.AutoMirrored.Filled.List)
    data object Pantry : TabItem("Pantry", Icons.Filled.Kitchen)
    data object Shopping : TabItem("Shopping", Icons.Filled.Checklist)
    data object Scanning : TabItem("Scanning", Icons.Filled.CameraAlt)
}

// ────────────────────────────────────────────────
// 2) MainScreen – without horizontal swiping
// ────────────────────────────────────────────────
// Replace with a valid API version if necessary
@Composable
fun MainScreen() {
    val editingGuard = remember { EditingGuard() }
    val tabs = listOf(TabItem.Recipes, TabItem.Pantry, TabItem.Shopping, TabItem.Scanning)
    var currentPage by remember { mutableIntStateOf(0) }
    val navMap = tabs.associateWith { rememberNavController() }

    CompositionLocalProvider(LocalEditingGuard provides editingGuard) {
        Scaffold { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                TabRow(selectedTabIndex = currentPage) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = currentPage == index,
                            onClick = {
                                if (!editingGuard.isEditing) {
                                    currentPage = index
                                } else {
                                    editingGuard.requestExit {
                                        currentPage = index
                                    }
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            text = { Text(tab.title) },
                            enabled = true // Keep enabled so it looks clickable, but logic is guarded
                        )
                    }
                }

                when (tabs[currentPage]) {
                    TabItem.Recipes -> RecipesNavHost(navMap[TabItem.Recipes]!!)
                    TabItem.Pantry -> PantryNavHost(navMap[TabItem.Pantry]!!)
                    TabItem.Shopping -> ShoppingNavHost(navMap[TabItem.Shopping]!!)
                    TabItem.Scanning -> ScanningNavHost(navMap[TabItem.Scanning]!!)
                }
            }
        }
    }

            if (editingGuard.showDiscardDialog) {
                AlertDialog(
                    onDismissRequest = { editingGuard.cancelExit() },
                    title = {
                        Text("Discard changes?", style = MaterialTheme.typography.headlineSmall)
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("You have unsaved changes. What would you like to do?")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Text(
                                    "Changes will be lost if you discard.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = { editingGuard.confirmExit() }, modifier = Modifier.fillMaxWidth()) {
                            Text("Discard Changes")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { editingGuard.cancelExit() }, modifier = Modifier.fillMaxWidth()) {
                            Text("Keep Editing")
                        }
                    },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

// ────────────────────────────────────────────────
// 3) NavHost definitions for your individual tabs.
// These can be moved to separate files if desired.
// ────────────────────────────────────────────────
@Composable
fun RecipesNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = "recipes_main") {
        composable("recipes_main") {
            RecipeScreenWithSearch(
                onRecipeClick = { recipe ->
                    navController.navigate("recipe_details/${recipe.id}")
                },
                onAddClick = {
                    navController.navigate("add_recipe")
                }
            )
        }

        composable("add_recipe") {
            RecipeCreationFormScreen(
                onRecipeCreated = {
                    navController.navigateUp()
                },
                onCancel = {
                    navController.navigateUp()
                }
            )
        }

        composable("recipe_details/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toLongOrNull()
            id?.let {
                RecipeDetailScreen(
                    recipeId = it,
                    viewModel = hiltViewModel(),
                    navController = navController
                )
            }
                ?: navController.navigateUp()
        }
    }
}


@Composable
fun PantryNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = "pantry_main") {
        composable("pantry_main") {
            val viewModel: PantryViewModel = hiltViewModel()
            PantryScreen(viewModel = viewModel)
        }

        // Optional future routes, like add/edit pantry item screens:
        // composable("add_item") { ... }
    }
}

@Composable
fun ShoppingNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = "shopping_main") {
        composable("shopping_main") {
            val viewModel: ShoppingListViewModel = hiltViewModel()
            val shoppingLists by viewModel.allShoppingLists.collectAsState()

            ShoppingMainScreen(
                shoppingLists = shoppingLists,
                onListClick = { list ->
                    viewModel.setActiveList(list.id)
                    navController.navigate("shopping_list/${list.id}")
                },
                onCreateList = { name, recipeIds, ingredientQuantities ->
                    viewModel.createListWithRecipesAndIngredients(
                        name = name,
                        recipeIds = recipeIds,
                        ingredientQuantities = ingredientQuantities
                    ) { newListId ->
                        navController.navigate("shopping_list/$newListId")
                    }
                },
                onDeleteList = { list ->
                    viewModel.deleteListWithItems(list)
                }
            )
        }

        composable("shopping_list/{listId}") { backStackEntry ->
            val viewModel: ShoppingListViewModel = hiltViewModel()
            val listId = backStackEntry.arguments?.getString("listId")?.toLongOrNull()
                ?: return@composable

            LaunchedEffect(listId) {
                viewModel.setActiveList(listId)
            }

            val categorizedItems by viewModel.categorizedItems.collectAsState()
            val allLists by viewModel.allShoppingLists.collectAsState()
            val listName = allLists.firstOrNull { it.id == listId }?.name ?: "Shopping List"

            ShoppingListScreen(
                navController = navController,
                listName = listName,
                categorizedItems = categorizedItems,
                onCheckToggled = viewModel::toggleCheck
            )
        }
    }
}




@Composable
fun ScanningNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = "scan_main") {
        composable("scan_main") {
            val viewModel: ScanViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            val shouldScan = !uiState.promptNewItemDialog &&
                    !uiState.promptLinkScanCodeDialog &&
                    !uiState.scanSuccess

            ScanningTab(
                shouldScan = shouldScan,
                onScanResult = { scannedCode ->
                    viewModel.scan(scannedCode)
                }
            )

            when {
                uiState.promptLinkScanCodeDialog && uiState.scannedItem != null -> {
                    LinkScanCodeDialog(
                        item = uiState.scannedItem!!,
                        onConfirmLink = { item ->
                            viewModel.linkScanCodeToItem(item, uiState.lastScanCode.orEmpty())
                        },
                        onDismiss = {
                            viewModel.clearScanResult()
                        }
                    )
                }

                uiState.scanSuccess && uiState.scannedItem != null -> {
                    UpdateItemDialog(
                        item = uiState.scannedItem!!,
                        onDismiss = {
                            viewModel.clearScanResult()
                        },
                        onConfirmUpdate = { updatedItem ->
                            viewModel.updateItem(updatedItem)
                        }
                    )
                }

                uiState.promptNewItemDialog -> {
                    CreateFromScanDialog(
                        scanCode = uiState.lastScanCode.orEmpty(),
                        categories = viewModel.allCategories.collectAsState().value,
                        selectedCategory = uiState.selectedCategory,
                        onCategoryChange = { category ->
                            viewModel.updateSelectedCategory(category)
                        },
                        onConfirm = { name, qty, imageData, categoryName ->
                            viewModel.addPantryItem(
                                PantryItem(
                                    name = name,
                                    quantity = qty,
                                    imageData = imageData,
                                    scanCode = uiState.lastScanCode.orEmpty(),
                                    category = categoryName ?: ""
                                )
                            )
                            viewModel.clearScanResult()
                        },
                        onDismiss = {
                            viewModel.clearScanResult()
                        }
                    )
                }
            }
        }
    }
}
