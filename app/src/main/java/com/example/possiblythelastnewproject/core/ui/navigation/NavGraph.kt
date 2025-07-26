package com.example.possiblythelastnewproject.core.ui.navigation

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
import com.example.possiblythelastnewproject.features.shoppingList.ui.componets.ShoppingMainScreen
import com.example.possiblythelastnewproject.features.shoppingList.ui.model.ShoppingListViewModel



// ────────────────────────────────────────────────
// @) NavHost definitions for your individual tabs.
// These can be moved to separate files if desired.
// ────────────────────────────────────────────────

@Composable
fun RecipesNavHost(
    navController: NavHostController,
    registerDiscardCleanup: (()->Unit) -> Unit
) {
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
                onRecipeCreated = { navController.navigateUp() },
                onCancel = { navController.navigateUp() },
                onDiscardRequested = registerDiscardCleanup // ✅ hand cleanup to MainScreen
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
            } ?: navController.navigateUp()
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
fun ShoppingNavHost(navHostController: NavHostController) {
    NavHost(
        navController = navHostController,
        startDestination = "shopping_main"
    ) {
        // 📝 List Overview + Create New
        composable("shopping_main") {
            val viewModel: ShoppingListViewModel = hiltViewModel()
            val shoppingLists by viewModel.allShoppingLists.collectAsState()

            ShoppingMainScreen(
                shoppingLists = shoppingLists,
                onListClick = { list ->
                    viewModel.setActiveListId(list.id)
                    navHostController.navigate("shopping_list/${list.id}")
                },
                onCreateList = { name ->
                    viewModel.createNewList(name) { newId ->
                        navHostController.navigate("shopping_list/$newId")
                    }
                },
                onDeleteList = viewModel::deleteList
            )
        }

        // 🛒 Shopping List Detail
        composable("shopping_list/{listId}") { backStackEntry ->
            val listId = backStackEntry.arguments?.getString("listId")?.toLongOrNull()
                ?: return@composable

            val viewModel: ShoppingListViewModel = hiltViewModel()

            LaunchedEffect(listId) {
                viewModel.setActiveListId(listId)
            }

            ShoppingListScreen(
                viewModel = viewModel,
                onBack = { navHostController.popBackStack() }
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
                        onConfirm = { name, qty, imageUri, categoryName ->
                            viewModel.addPantryItem(
                                PantryItem(
                                    name = name,
                                    quantity = qty,
                                    imageUri = imageUri?.toString(),
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
