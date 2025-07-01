package com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeCreation

import android.graphics.BitmapFactory
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.possiblythelastnewproject.core.ui.LocalEditingGuard
import com.example.possiblythelastnewproject.core.ui.theme.PossiblyTheLastNewProjectTheme
import com.example.possiblythelastnewproject.core.utils.imagePicker
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.pantry.ui.PantryViewModel
import com.example.possiblythelastnewproject.features.recipe.data.entities.Recipe
import com.example.possiblythelastnewproject.features.recipe.ui.RecipesViewModel
import com.example.possiblythelastnewproject.features.recipe.ui.componets.ingredientChips.IngredientChipEditor

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun PreviewRecipeCreationFormScreen() {
    PossiblyTheLastNewProjectTheme {
        RecipeCreationFormScreen(onRecipeCreated = {}, onCancel = {})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeCreationFormScreen(
    onRecipeCreated: (Recipe) -> Unit,
    onCancel: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val recipeViewModel: RecipesViewModel = hiltViewModel()
    val pantryViewModel: PantryViewModel = hiltViewModel()
    val pantryItems by pantryViewModel.allItems.collectAsState(emptyList())
    val editingGuard = LocalEditingGuard.current

    var name by remember { mutableStateOf("") }
    var temp by remember { mutableStateOf("") }
    var prepTime by remember { mutableStateOf("") }
    var cookTime by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var cardColor by remember { mutableStateOf(Color(0xFFF44336)) }
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var ingredientList by remember { mutableStateOf(mutableListOf<RecipeIngredientUI>()) }

    val launchImagePicker = imagePicker { imageBytes = it }

    val colorOptions = listOf(
        0xFFF44336, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7,
        0xFF3F51B5, 0xFF2196F3, 0xFF03A9F4, 0xFF00BCD4,
        0xFF009688, 0xFF4CAF50, 0xFFFFC107, 0xFFFF5722
    ).map { Color(it or 0xFF000000) }

    fun hasUnsavedChanges(): Boolean {
        return name.isNotBlank() || temp.isNotBlank() || prepTime.isNotBlank() ||
                cookTime.isNotBlank() || category.isNotBlank() || instructions.isNotBlank() ||
                imageBytes?.isNotEmpty() == true || ingredientList.isNotEmpty()
    }

    // Sync editing state with EditingGuard
    LaunchedEffect(
        name, temp, prepTime, cookTime, category,
        instructions, imageBytes, ingredientList
    ) {
        editingGuard.isEditing = hasUnsavedChanges()
    }
    DisposableEffect(Unit) {
        onDispose {
            editingGuard.isEditing = false
        }
    }
    BackHandler(enabled = editingGuard.isEditing) {
        if (hasUnsavedChanges()) {
            editingGuard.requestExit {
                editingGuard.isEditing = false
                onCancel()
            }
        } else {
            onCancel()
        }


    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Recipe") },
                navigationIcon = {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        if (hasUnsavedChanges()) {
                            editingGuard.requestExit {
                                editingGuard.isEditing = false
                                onCancel()
                            }
                        } else {
                            onCancel()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .animateContentSize(tween(300)),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RecipeImagePicker(imageBytes) { launchImagePicker() }

            RecipeFormCard(
                name = name,
                onNameChange = { name = it },
                temp = temp,
                onTempChange = { temp = it },
                prepTime = prepTime,
                onPrepTimeChange = { prepTime = it },
                cookTime = cookTime,
                onCookTimeChange = { cookTime = it },
                category = category,
                onCategoryChange = { category = it },
                cardColor = cardColor,
                onCardColorChange = { cardColor = it },
                colorOptions = colorOptions,
                ingredients = ingredientList,
                onIngredientsChange = { ingredientList = it.toMutableList() },
                instructions = instructions,
                onInstructionsChange = { instructions = it },
                onSave = {
                    focusManager.clearFocus()
                    val recipe = Recipe(
                        id = 0L,
                        name = name.trim(),
                        temp = temp.trim(),
                        prepTime = prepTime.trim(),
                        cookTime = cookTime.trim(),
                        category = category.trim(),
                        instructions = instructions.trim(),
                        imageData = imageBytes ?: ByteArray(0),
                        color = cardColor.toArgb()
                    )
                    recipeViewModel.saveRecipeWithIngredientsUi(recipe, ingredientList)
                    editingGuard.isEditing = false
                    onRecipeCreated(recipe)
                },
                onCancel = {
                    focusManager.clearFocus()
                    if (hasUnsavedChanges()) {
                        editingGuard.requestExit {
                            editingGuard.isEditing = false
                            onCancel()
                        }
                    } else {
                        onCancel()
                    }
                },
                pantryItems = pantryItems,
                onRequestCreatePantryItem = { pantryViewModel.insertAndReturn(it) },
                onToggleShoppingStatus = { pantryViewModel.update(it) }
            )
        }
    }
}

@Composable
private fun RecipeImagePicker(imageBytes: ByteArray?, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(onClick = onClick)
    ) {
        imageBytes?.takeIf { it.isNotEmpty() }?.let { bytes ->
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Recipe image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text("Tap to select image", color = Color.DarkGray)
        }
    }
}

@Composable
private fun RecipeFormCard(
    name: String,
    onNameChange: (String) -> Unit,
    temp: String,
    onTempChange: (String) -> Unit,
    prepTime: String,
    onPrepTimeChange: (String) -> Unit,
    cookTime: String,
    onCookTimeChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    cardColor: Color,
    onCardColorChange: (Color) -> Unit,
    colorOptions: List<Color>,
    ingredients: List<RecipeIngredientUI>,
    onIngredientsChange: (List<RecipeIngredientUI>) -> Unit,
    instructions: String,
    onInstructionsChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    pantryItems: List<PantryItem>,
    onRequestCreatePantryItem: suspend (String) -> PantryItem,
    onToggleShoppingStatus: (PantryItem) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FormField("Recipe Name", name, onNameChange)
            FormField("Temperature", temp, onTempChange)
            FormField("Prep Time", prepTime, onPrepTimeChange)
            FormField("Cook Time", cookTime, onCookTimeChange)
            FormField("Category", category, onCategoryChange)

            Text("Card Color", style = MaterialTheme.typography.labelMedium)
            ColorPicker(
                selectedColor = cardColor,
                colorOptions = colorOptions,
                onColorSelected = onCardColorChange
            )

            Text("Ingredients", style = MaterialTheme.typography.labelMedium)
            IngredientChipEditor(
                ingredients = ingredients,
                onIngredientsChange = onIngredientsChange,
                allPantryItems = pantryItems,
                onRequestCreatePantryItem = onRequestCreatePantryItem,
                onToggleShoppingStatus = onToggleShoppingStatus
            )

            FormField(
                label = "Instructions",
                value = instructions,
                onValueChange = onInstructionsChange,
                singleLine = false,
                heightDp = 140
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }

                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}