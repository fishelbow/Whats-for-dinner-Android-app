package com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeDetail

import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.possiblythelastnewproject.core.ui.LocalEditingGuard
import com.example.possiblythelastnewproject.core.utils.imagePicker
import com.example.possiblythelastnewproject.features.pantry.ui.PantryViewModel
import com.example.possiblythelastnewproject.features.recipe.data.RecipeWithIngredients
import com.example.possiblythelastnewproject.features.recipe.ui.RecipesViewModel
import com.example.possiblythelastnewproject.features.recipe.ui.componets.ingredientChips.IngredientChipEditor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: Long,
    navController: NavHostController,
    viewModel: RecipesViewModel = hiltViewModel()
) {
    val pantryViewModel: PantryViewModel = hiltViewModel()
    val pantryItems by pantryViewModel.allItems.collectAsState()
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val editingGuard = LocalEditingGuard.current

    var showDuplicateNameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }



    val initialData = produceState<RecipeWithIngredients?>(null, recipeId) {
        value = viewModel.getRecipeWithIngredients(recipeId)
    }.value ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val uiState = viewModel.editUiState
    val ingredients by viewModel.observeIngredientsForRecipe(
        recipeId = initialData.recipe.id,
        pantryItems = pantryItems
    ).collectAsState(initial = emptyList())


    val hasChanges = remember(uiState) {
        derivedStateOf { viewModel.hasUnsavedChanges(initialData) }
    }
    val initialized = remember { mutableStateOf(false) }

    LaunchedEffect(recipeId) {
        viewModel.getRecipeWithIngredients(recipeId)?.let {
            viewModel.loadRecipeIntoUiState(it)
            initialized.value = true
        }
    }


    DisposableEffect(Unit) {
        onDispose { editingGuard.isEditing = false }
    }

    suspend fun refreshUiFromDb() {
        viewModel.getRecipeWithIngredients(recipeId)?.let {
            viewModel.loadRecipeIntoUiState(it)
            editingGuard.isEditing = false
        }
    }

    BackHandler(enabled = editingGuard.isEditing) {
        if (viewModel.hasUnsavedChanges(initialData)) {
            editingGuard.requestExit {
                coroutineScope.launch { refreshUiFromDb() }
            }
        } else {
            navController.navigateUp()
        }
    }

    val pickImage = imagePicker { byteArray ->
        viewModel.updateImageData(byteArray)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recipe Details") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (editingGuard.isEditing && viewModel.hasUnsavedChanges(initialData)) {
                            editingGuard.requestExit {
                                coroutineScope.launch { refreshUiFromDb() }
                            }
                        } else if (editingGuard.isEditing) {
                            editingGuard.isEditing = false
                        } else {
                            navController.navigateUp()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (editingGuard.isEditing) "Cancel edit" else "Back"
                        )
                    }
                },
                actions = {
                    if (!editingGuard.isEditing) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                        TextButton(
                            onClick = { editingGuard.isEditing = true },
                            enabled = initialized.value
                        ) {
                            Text("Edit")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .padding(16.dp)
                .animateContentSize(tween(300)),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Image Picker
            Surface(
                shape = RoundedCornerShape(8.dp),
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable(enabled = editingGuard.isEditing) { pickImage() }
            ) {
                uiState.imageData?.takeIf { it.isNotEmpty() }?.let { data ->
                    val bmp = BitmapFactory.decodeByteArray(data, 0, data.size)
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Recipe photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } ?: Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (editingGuard.isEditing) "Tap to select image" else "No image",
                        color = Color.DarkGray
                    )
                }
            }

            // Details Card
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!editingGuard.isEditing) {
                        ReadOnlyField("Name", uiState.name.text)
                        ReadOnlyField("Temperature", uiState.temp.text)
                        ReadOnlyField("Prep Time", uiState.prepTime.text)
                        ReadOnlyField("Cook Time", uiState.cookTime.text)
                        ReadOnlyField("Category", uiState.category.text)

                        Text("Ingredients", style = MaterialTheme.typography.labelMedium)
                        when {
                            !initialized.value -> {
                                Box(
                                    Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }

                            ingredients.isEmpty() -> {
                                Text(
                                    text = "No ingredients yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            else -> {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val pantryItemsById = pantryItems.associateBy { it.id }
                                    ingredients.forEach { item ->
                                        val pantryItem = pantryItemsById[item.pantryItemId]
                                        val isShoppable = pantryItem?.addToShoppingList == true
                                        AssistChip(
                                            onClick = {},
                                            label = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("${item.amountRequired}×${item.name}")
                                                    if (isShoppable) {
                                                        Icon(
                                                            imageVector = Icons.Default.ShoppingCart,
                                                            contentDescription = "In Shopping List",
                                                            modifier = Modifier
                                                                .size(16.dp)
                                                                .padding(start = 4.dp),
                                                            tint = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider()
                        ReadOnlyField("Instructions", uiState.instructions.text)

                        Text("Card Color", style = MaterialTheme.typography.labelMedium)
                        Box(
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(uiState.cardColor)
                                .border(1.dp, Color.Black, CircleShape)
                        )
                    } else {
                        EditableField("Name", uiState.name) { viewModel.updateName(it) }
                        EditableField("Temperature", uiState.temp) { viewModel.updateTemp(it) }
                        EditableField("Prep Time", uiState.prepTime) { viewModel.updatePrepTime(it) }
                        EditableField("Cook Time", uiState.cookTime) { viewModel.updateCookTime(it) }
                        EditableField("Category", uiState.category) { viewModel.updateCategory(it) }

                        IngredientChipEditor(
                            ingredients = uiState.ingredients,
                            onIngredientsChange = { viewModel.updateIngredients(it) },
                            allPantryItems = pantryItems,
                            onRequestCreatePantryItem = { pantryViewModel.insertAndReturn(it) },
                            onToggleShoppingStatus = { updatedItem -> pantryViewModel.update(updatedItem) }
                        )

                        HorizontalDivider()

                        EditableField(
                            label = "Instructions",
                            state = uiState.instructions,
                            singleLine = false,
                            heightDp = 140,
                            onValueChange = { viewModel.updateInstructions(it) }
                        )

                        // Color Picker
                        val colorOptions = listOf(
                            0xFFF44336, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7,
                            0xFF3F51B5, 0xFF2196F3, 0xFF03A9F4, 0xFF00BCD4,
                            0xFF009688, 0xFF4CAF50, 0xFFFFC107, 0xFFFF5722
                        ).map { Color(it) }

                        Text("Card Color", style = MaterialTheme.typography.labelMedium)
                        colorOptions.chunked(6).forEach { row ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                            ) {
                                row.forEach { col ->
                                    Box(
                                        Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(col)
                                            .border(
                                                width = if (col == uiState.cardColor) 3.dp else 1.dp,
                                                color = if (col == uiState.cardColor)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    Color.Gray,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                viewModel.updateCardColor(col)
                                            }
                                    )
                                }
                            }
                        }

                        HorizontalDivider()

                        // Save / Cancel Buttons
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val updated = initialData.recipe.copy(
                                        name = uiState.name.text.trim(),
                                        temp = uiState.temp.text.trim(),
                                        prepTime = uiState.prepTime.text.trim(),
                                        cookTime = uiState.cookTime.text.trim(),
                                        category = uiState.category.text.trim(),
                                        imageData = uiState.imageData,
                                        instructions = uiState.instructions.text.trim(),
                                        color = uiState.cardColor.toArgb()
                                    )

                                    coroutineScope.launch {
                                        val isDuplicate = viewModel.recipeNameExists(
                                            name = updated.name,
                                            excludeUuid = updated.uuid
                                        )
                                        if (isDuplicate) {
                                            showDuplicateNameDialog = true
                                            return@launch
                                        }

                                        viewModel.updateRecipeWithIngredientsUi(
                                            updated,
                                            uiState.ingredients
                                        )
                                        editingGuard.isEditing = false
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save")
                            }

                            OutlinedButton(
                                onClick = {
                                    if (viewModel.hasUnsavedChanges(initialData)) {
                                        editingGuard.requestExit {
                                            coroutineScope.launch { refreshUiFromDb() }
                                        }
                                    } else {
                                        editingGuard.isEditing = false
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Recipe") },
            text = { Text("Are you sure? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(initialData.recipe)
                    showDeleteDialog = false
                    navController.navigateUp()
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Duplicate name warning dialog
    if (showDuplicateNameDialog) {
        AlertDialog(
            onDismissRequest = { showDuplicateNameDialog = false },
            title = { Text("Duplicate Recipe Name") },
            text = { Text("Another recipe already uses this name. Please choose a different one.") },
            confirmButton = {
                TextButton(onClick = { showDuplicateNameDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}