package com.example.possiblythelastnewproject.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.possiblythelastnewproject.features.pantry.data.PantryItem
import com.example.possiblythelastnewproject.features.pantry.ui.PantryScreen
import com.example.possiblythelastnewproject.features.pantry.ui.PantryViewModel
import com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeDetail.RecipeDetailScreen
import com.example.possiblythelastnewproject.features.recipe.ui.componets.RecipeScreenWithSearch
import com.example.possiblythelastnewproject.features.recipe.ui.RecipesViewModel
import com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeCreation.RecipeCreationFormScreen
import com.example.possiblythelastnewproject.features.scan.ui.ScanViewModel
import com.example.possiblythelastnewproject.features.scan.ui.ScanningTab
import com.example.possiblythelastnewproject.features.scan.ui.components.CreateFromScanDialog
import com.example.possiblythelastnewproject.features.scan.ui.componets.LinkScanCodeDialog
import com.example.possiblythelastnewproject.features.scan.ui.componets.UpdateItemDialog

// ────────────────────────────────────────────────
// 1) Tab model
// ────────────────────────────────────────────────
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
    // List of tabs
    val tabs = listOf(TabItem.Recipes, TabItem.Pantry, TabItem.Shopping, TabItem.Scanning)
    // Use a mutable state to track the currently selected tab index
    var currentPage by remember { mutableStateOf(0) }
    // Associate a NavController to each tab
    val navMap = tabs.associateWith { rememberNavController() }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // TabRow displays the list of tabs.
            TabRow(selectedTabIndex = currentPage) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = currentPage == index,
                        onClick = { currentPage = index },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        text = { Text(tab.title) }
                    )
                }
            }

            // Display the NavHost corresponding to the selected tab
            when (tabs[currentPage]) {
                TabItem.Recipes -> RecipesNavHost(navMap[TabItem.Recipes]!!)
                TabItem.Pantry -> PantryNavHost(navMap[TabItem.Pantry]!!)
                TabItem.Shopping -> ShoppingNavHost(navMap[TabItem.Shopping]!!)
                TabItem.Scanning -> ScanningNavHost(navMap[TabItem.Scanning]!!)
            }
        }
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
            // grab the same VM you use elsewhere
            val viewModel: RecipesViewModel = hiltViewModel()

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
        composable("shopping_main") { ScreenContent(title = "Shopping Main") }
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
                        onConfirm = { name, qty, imageData ->
                            viewModel.handleManualNameEntry(
                                name = name,
                                pendingScanCode = uiState.lastScanCode.orEmpty(),
                                quantity = qty,
                                imageData = imageData
                            )
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

// ────────────────────────────────────────────────
// 4) Reusable ScreenContent Composable
// ────────────────────────────────────────────────
@Composable
fun ScreenContent(title: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen()
}