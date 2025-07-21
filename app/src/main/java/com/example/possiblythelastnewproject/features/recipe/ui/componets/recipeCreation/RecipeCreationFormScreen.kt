package com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeCreation

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.possiblythelastnewproject.features.recipe.ui.componets.LocalEditingGuard
import com.example.possiblythelastnewproject.core.utils.deleteImageFromStorage
import com.example.possiblythelastnewproject.core.utils.imagePicker
import com.example.possiblythelastnewproject.features.pantry.ui.PantryViewModel
import com.example.possiblythelastnewproject.features.recipe.data.entities.Recipe
import com.example.possiblythelastnewproject.features.recipe.ui.RecipesViewModel
import com.example.possiblythelastnewproject.features.recipe.ui.componets.mainScreen.RecipeImagePicker
import kotlinx.coroutines.launch

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeCreationFormScreen(
    onRecipeCreated: (Recipe) -> Unit,
    onCancel: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val recipeViewModel: RecipesViewModel = hiltViewModel()
    val pantryViewModel: PantryViewModel = hiltViewModel()
    val pantryItems by pantryViewModel.allItems.collectAsState(emptyList())
    val editingGuard = LocalEditingGuard.current
    val isEditing by remember { derivedStateOf { editingGuard.isEditing } }
    var name by remember { mutableStateOf("") }
    var temp by remember { mutableStateOf("") }
    var prepTime by remember { mutableStateOf("") }
    var cookTime by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var cardColor by remember { mutableStateOf(Color(0xFFF44336)) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val pickedImageUris = remember { mutableStateListOf<String>() }
    var ingredientList by remember { mutableStateOf(mutableListOf<RecipeIngredientUI>()) }

    var showNameRequiredDialog by remember { mutableStateOf(false) }
    var showDuplicateDialog by remember { mutableStateOf(false) }
    var lastSavedImagePath by remember { mutableStateOf<String?>(null) }
    val colorOptions = listOf(
        0xFFF44336, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7,
        0xFF3F51B5, 0xFF2196F3, 0xFF03A9F4, 0xFF00BCD4,
        0xFF009688, 0xFF4CAF50, 0xFFFFC107, 0xFFFF5722
    ).map { Color(it or 0xFF000000) }

    fun hasUnsavedChanges(): Boolean {
        return name.isNotBlank() || temp.isNotBlank() || prepTime.isNotBlank() ||
                cookTime.isNotBlank() || category.isNotBlank() || instructions.isNotBlank() ||
                imageUri != null || ingredientList.isNotEmpty()
    }

    LaunchedEffect(Unit) {
        editingGuard.isEditing = false
    }

    // 🔐 Sync edit state
    LaunchedEffect(
        name, temp, prepTime, cookTime, category,
        instructions, imageUri, ingredientList
    ) {
        editingGuard.isEditing = hasUnsavedChanges()
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                editingGuard.isEditing = hasUnsavedChanges()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 🧼 Shared cancellation logic with image deletion
    fun handleCancel() {
        focusManager.clearFocus()

        pickedImageUris.forEach { uriStr ->
            deleteImageFromStorage(uriStr, context)
        }
        pickedImageUris.clear()
        imageUri = null

        editingGuard.isEditing = false
        onCancel()
    }

    val backHandlerKey = remember { mutableStateOf(0) }

    LaunchedEffect(isEditing) {
        // trigger recomposition of BackHandler when guard changes
        backHandlerKey.value++
    }

    key(backHandlerKey.value) {
        BackHandler(enabled = isEditing) {
            if (hasUnsavedChanges()) {
                editingGuard.requestExit(
                    rollback = {
                        // optional rollback logic, e.g. reset image or field state
                    },
                    thenExit = {
                        handleCancel()
                    }
                )
            } else {
                handleCancel()
            }
        }
    }

    val launchImagePicker = imagePicker { selectedUri ->
        editingGuard.isEditing = true
        selectedUri?.let {
            val newPath = it.toString()

            // 🧹 Clean up old image before overwrite
            lastSavedImagePath?.let { previous ->
                if (previous != newPath) {
                    deleteImageFromStorage(previous, context)
                }
            }

            pickedImageUris += newPath
            lastSavedImagePath = newPath
            imageUri = it // This should happen last
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Recipe") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasUnsavedChanges()) {
                            editingGuard.requestExit(
                                rollback = {
                                    // Optional rollback logic — e.g. delete unsaved image, reset fields
                                },
                                thenExit = {
                                    handleCancel()
                                }
                            )
                        } else {
                            handleCancel()
                        }
                    }){
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
            RecipeImagePicker(imageUri) { launchImagePicker() }

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
                    val trimmedName = name.trim()

                    if (trimmedName.isBlank()) {
                        showNameRequiredDialog = true
                        return@RecipeFormCard
                    }

                    scope.launch {
                        if (recipeViewModel.recipeNameExists(trimmedName)) {
                            showDuplicateDialog = true
                            return@launch
                        }

                        val recipe = Recipe(
                            id = 0L,
                            name = trimmedName,
                            temp = temp.trim(),
                            prepTime = prepTime.trim(),
                            cookTime = cookTime.trim(),
                            category = category.trim(),
                            instructions = instructions.trim(),
                            imageUri = imageUri?.toString() ?: "",
                            color = cardColor.toArgb()
                        )

                        recipeViewModel.saveRecipeWithIngredientsUi(
                            recipe, ingredientList,
                            context = context
                        )
                        editingGuard.isEditing = false
                        onRecipeCreated(recipe)
                    }
                },
                onCancel = {
                    if (hasUnsavedChanges() && editingGuard.isEditing) {
                        editingGuard.requestExit(
                            rollback = {
                                // Optional: cleanup logic if form has unsaved image or other state
                            },
                            thenExit = {
                                handleCancel()
                            }
                        )
                    } else {
                        handleCancel()
                    }
                },
                pantryItems = pantryItems,
                onRequestCreatePantryItem = { pantryViewModel.insertAndReturn(it) },
                onToggleShoppingStatus = { item ->
                    pantryViewModel.update(
                        item.copy(addToShoppingList = !item.addToShoppingList),
                        oldImageUri = item.imageUri ?: "",
                        context = context
                    )
                }
            )
        }
    }

    // 📦 Dialogs
    if (showDuplicateDialog) {
        AlertDialog(
            onDismissRequest = { showDuplicateDialog = false },
            title = { Text("Duplicate Recipe Name") },
            text = { Text("A recipe with this name already exists. Please choose a different name.") },
            confirmButton = {
                TextButton(onClick = { showDuplicateDialog = false }) { Text("OK") }
            }
        )
    }

    if (showNameRequiredDialog) {
        AlertDialog(
            onDismissRequest = { showNameRequiredDialog = false },
            title = { Text("Missing Name") },
            text = { Text("Please enter a name for the recipe before saving.") },
            confirmButton = {
                TextButton(onClick = { showNameRequiredDialog = false }) { Text("OK") }
            }
        )
    }
}