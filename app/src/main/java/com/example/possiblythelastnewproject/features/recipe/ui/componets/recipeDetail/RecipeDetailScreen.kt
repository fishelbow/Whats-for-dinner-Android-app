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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.possiblythelastnewproject.core.ui.LocalEditingGuard
import com.example.possiblythelastnewproject.core.utils.imagePicker
import com.example.possiblythelastnewproject.features.pantry.ui.PantryViewModel
import com.example.possiblythelastnewproject.features.recipe.data.RecipeWithIngredients
import com.example.possiblythelastnewproject.features.recipe.ui.RecipesViewModel
import com.example.possiblythelastnewproject.features.recipe.ui.componets.ingredientChips.IngredientChipEditor
import com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeCreation.RecipeIngredientUI
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
    val initialData = produceState<RecipeWithIngredients?>(null, recipeId) {
        value = viewModel.getRecipeWithIngredients(recipeId)
    }.value ?: run {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Local UI state
    var name by remember { mutableStateOf(TextFieldValue(initialData.recipe.name)) }
    var temp by remember { mutableStateOf(TextFieldValue(initialData.recipe.temp)) }
    var prepTime by remember { mutableStateOf(TextFieldValue(initialData.recipe.prepTime)) }
    var cookTime by remember { mutableStateOf(TextFieldValue(initialData.recipe.cookTime)) }
    var category by remember { mutableStateOf(TextFieldValue(initialData.recipe.category)) }
    var newIngredient by remember { mutableStateOf("") }
    var ingredientList by remember { mutableStateOf(emptyList<RecipeIngredientUI>()) }
    var instructions by remember { mutableStateOf(TextFieldValue(initialData.recipe.instructions)) }
    var cardColor by remember { mutableStateOf(Color(initialData.recipe.color)) }
    var imageData by remember { mutableStateOf(initialData.recipe.imageData) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingExitAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val recipe = initialData.recipe

    val ingredients by viewModel.observeIngredientsForRecipe(
        recipeId = recipe.id,
        pantryItems = pantryItems
    ).collectAsState(initial = emptyList())

    val initialized = remember { mutableStateOf(false) }

    LaunchedEffect(recipeId) {
        val result = viewModel.getRecipeWithIngredients(recipeId)
        ingredientList = result?.ingredients?.map {
            RecipeIngredientUI(
                name = it.name,
                pantryItemId = it.id,
                isShoppable = false,
                hasScanCode = false
            )
        } ?: emptyList()
        initialized.value = true
    }

    LaunchedEffect(recipeId) {
        initialized.value = false
    }
    fun hasUnsavedChanges(): Boolean {
        return editingGuard.isEditing && (
                name.text != initialData.recipe.name ||
                        temp.text != initialData.recipe.temp ||
                        prepTime.text != initialData.recipe.prepTime ||
                        cookTime.text != initialData.recipe.cookTime ||
                        category.text != initialData.recipe.category ||
                        instructions.text != initialData.recipe.instructions ||
                        cardColor.toArgb() != initialData.recipe.color ||
                        imageData?.contentEquals(initialData.recipe.imageData) == false ||
                        ingredientList.map { it.name } != ingredients.map { it.name }
                )
    }

    LaunchedEffect(
        name.text, temp.text, prepTime.text, cookTime.text,
        category.text, instructions.text, cardColor, imageData, ingredientList
    ) {
        editingGuard.isEditing = hasUnsavedChanges()
    }

    DisposableEffect(Unit) {
        onDispose {
            editingGuard.isEditing = false
        }
    }

    LaunchedEffect(ingredients) {
        if (editingGuard.isEditing && ingredientList.isEmpty()) {
            ingredientList = ingredients
        }
    }

    suspend fun refreshUiFromDb() {
        viewModel.getRecipeWithIngredients(recipeId)?.let { refreshed ->
            val r = refreshed.recipe
            name = TextFieldValue(r.name)
            temp = TextFieldValue(r.temp)
            prepTime = TextFieldValue(r.prepTime)
            cookTime = TextFieldValue(r.cookTime)
            category = TextFieldValue(r.category)
            instructions = TextFieldValue(r.instructions)
            cardColor = Color(r.color)
            imageData = r.imageData
            newIngredient = ""
            ingredientList = refreshed.ingredients.map {
                RecipeIngredientUI(
                    name = it.name,
                    pantryItemId = it.id,
                    isShoppable = false,
                    hasScanCode = false
                )
            }
            editingGuard.isEditing = false
        }
    }

    BackHandler(enabled = editingGuard.isEditing) {
        if (hasUnsavedChanges()) {
            editingGuard.requestExit {
                coroutineScope.launch { refreshUiFromDb() }
            }
        } else {
            navController.navigateUp()
        }
    }

    val pickImage = imagePicker { byteArray -> imageData = byteArray }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recipe Details") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (editingGuard.isEditing && hasUnsavedChanges()) {
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
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .padding(16.dp)
                .animateContentSize(tween(300)),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Image picker
            Surface(
                shape = RoundedCornerShape(8.dp),
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable(enabled = editingGuard.isEditing) { pickImage() }
            ) {
                if (imageData?.isNotEmpty() == true) {
                    val bmp = BitmapFactory.decodeByteArray(imageData, 0, imageData!!.size)
                    Image(
                        bmp.asImageBitmap(),
                        contentDescription = "Recipe photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        Modifier.fillMaxSize().background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (editingGuard.isEditing) "Tap to select image" else "No image",
                            color = Color.DarkGray
                        )
                    }
                }
            }

            // Details card
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
                        ReadOnlyField("Name", name.text)
                        ReadOnlyField("Temperature", temp.text)
                        ReadOnlyField("Prep Time", prepTime.text)
                        ReadOnlyField("Cook Time", cookTime.text)
                        ReadOnlyField("Category", category.text)
                        Text("Ingredients", style = MaterialTheme.typography.labelMedium)

                        when {
                            !initialized.value -> {
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
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
                                                            modifier = Modifier.size(16.dp)
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
                        ReadOnlyField("Instructions", instructions.text)
                        Text("Card Color", style = MaterialTheme.typography.labelMedium)
                        Box(
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(cardColor)
                                .border(1.dp, Color.Black, CircleShape)
                        )
                    } else {
                        EditableField("Name", name) { name = it }
                        EditableField("Temperature", temp) { temp = it }
                        EditableField("Prep Time", prepTime) { prepTime = it }
                        EditableField("Cook Time", cookTime) { cookTime = it }
                        EditableField("Category", category) { category = it }

                        IngredientChipEditor(
                            ingredients = ingredientList,
                            onIngredientsChange = { ingredientList = it },
                            allPantryItems = pantryItems,
                            onRequestCreatePantryItem = { pantryViewModel.insertAndReturn(it) },
                            onToggleShoppingStatus = { updatedItem ->
                                pantryViewModel.update(
                                    updatedItem
                                )
                            }
                        )

                        HorizontalDivider()

                        EditableField(
                            "Instructions",
                            instructions,
                            singleLine = false,
                            heightDp = 140
                        ) {
                            instructions = it
                        }

                        // Color picker
                        val colorOptions = listOf(
                            0xFFF44336, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7,
                            0xFF3F51B5, 0xFF2196F3, 0xFF03A9F4, 0xFF00BCD4,
                            0xFF009688, 0xFF4CAF50, 0xFFFFC107, 0xFFFF5722
                        ).map { Color(it) }

                        Text("Card Color", style = MaterialTheme.typography.labelMedium)
                        colorOptions.chunked(6).forEach { row ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(
                                    8.dp,
                                    Alignment.CenterHorizontally
                                )
                            ) {
                                row.forEach { col ->
                                    Box(
                                        Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(col)
                                            .border(
                                                width = if (col == cardColor) 3.dp else 1.dp,
                                                color = if (col == cardColor) MaterialTheme.colorScheme.primary else Color.Gray,
                                                shape = CircleShape
                                            )
                                            .clickable { cardColor = col }
                                    )
                                }
                            }
                        }
                        HorizontalDivider()

                        // Save / Cancel buttons
                        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val updated = initialData.recipe.copy(
                                        name = name.text.trim(),
                                        temp = temp.text.trim(),
                                        prepTime = prepTime.text.trim(),
                                        cookTime = cookTime.text.trim(),
                                        category = category.text.trim(),
                                        imageData = imageData,
                                        instructions = instructions.text.trim(),
                                        color = cardColor.toArgb()
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

                                        viewModel.updateRecipeWithIngredientsUi(updated, ingredientList)
                                        editingGuard.isEditing = false
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save")
                            }

                            OutlinedButton(
                                onClick = {
                                    if (hasUnsavedChanges()) {
                                        editingGuard.requestExit {
                                            coroutineScope.launch {
                                                refreshUiFromDb()
                                            }
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