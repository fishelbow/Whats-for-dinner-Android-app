package com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeDetail

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.possiblythelastnewproject.core.ui.LocalEditingGuard
import com.example.possiblythelastnewproject.core.utils.imagePicker
import com.example.possiblythelastnewproject.features.pantry.ui.PantryViewModel
import com.example.possiblythelastnewproject.features.recipe.data.RecipeWithIngredients
import com.example.possiblythelastnewproject.features.recipe.ui.RecipesViewModel
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import com.example.possiblythelastnewproject.core.utils.deleteImageFromStorage
import com.example.possiblythelastnewproject.features.recipe.data.entities.RecipePantryItemCrossRef

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: Long,
    navController: NavHostController,
    viewModel: RecipesViewModel = hiltViewModel(),
    pantryViewModel: PantryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pantryItems by pantryViewModel.allItems.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val editingGuard = LocalEditingGuard.current
    val scrollState = rememberScrollState()

    var originalImageUri by remember { mutableStateOf("") }
    var lastSavedImagePath by remember { mutableStateOf<String?>(null) }
    var isUnsavedImageChange by remember { mutableStateOf(false) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDuplicateNameDialog by remember { mutableStateOf(false) }
    var showNameRequiredDialog by remember { mutableStateOf(false) }

    val initialized = remember { mutableStateOf(false) }

    // 🧭 Load initial data
    LaunchedEffect(recipeId) {
        val recipe = viewModel.getRecipeWithIngredients(recipeId)
        val crossRefs = viewModel.ingredientRepository.getCrossRefsForRecipeOnce(recipeId)
        recipe?.let {
            uiState.applyRecipe(it, crossRefs)
            originalImageUri = it.recipe.imageUri ?: ""
            lastSavedImagePath = originalImageUri
            initialized.value = true
        }
    }

    // 🔄 Reactive snapshot + crossRefs
    val crossRefsState by produceState(
        initialValue = emptyList<RecipePantryItemCrossRef>(),
        key1 = initialized.value
    ) {
        value = if (initialized.value) {
            viewModel.ingredientRepository.getCrossRefsForRecipeOnce(recipeId)
        } else emptyList()
    }

    val recipeSnapshotState by produceState<RecipeWithIngredients?>(
        initialValue = null,
        key1 = initialized.value
    ) {
        value = if (initialized.value) {
            viewModel.getRecipeWithIngredients(recipeId)
        } else null
    }

    // 🧠 Change detection
    val hasChangesState by remember(
        uiState,
        isUnsavedImageChange,
        crossRefsState,
        recipeSnapshotState,
        originalImageUri
    ) {
        derivedStateOf {
            val snapshot = recipeSnapshotState
            val crossRefs = crossRefsState
            val changed = snapshot?.let {
                uiState.isChangedFrom(it, crossRefs, originalImageUri)
            } ?: false
            changed || isUnsavedImageChange
        }
    }

    // 📸 Image Picker
    val pickImage = imagePicker { selectedUri ->
        editingGuard.isEditing = true
        selectedUri?.toString()?.let { newPath ->
            val currentStoredImage = uiState.imageUri.orEmpty()
            if (currentStoredImage.isNotEmpty() && currentStoredImage != newPath) {
                deleteImageFromStorage(currentStoredImage, context)
                Log.d("ImageCleanup", "🧨 Deleted replaced image → $currentStoredImage")
            }
            viewModel.updateImageUri(newPath)
            lastSavedImagePath = newPath
            if (newPath != originalImageUri) {
                isUnsavedImageChange = true
                Log.d("ImageChangeTracker", "📸 Image changed → unsaved=true")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recipe Details") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                val original = viewModel.getRecipeWithIngredients(recipeId)
                                val freshRefs = viewModel.ingredientRepository.getCrossRefsForRecipeOnce(recipeId)
                                val hasChanges = original?.let {
                                    uiState.isChangedFrom(it, freshRefs, originalImageUri)
                                } ?: false

                                if (editingGuard.isEditing && hasChanges) {
                                    editingGuard.requestExit {
                                        original?.let {
                                            uiState.rollbackImageIfNeeded(originalImageUri, context, ::deleteImageFromStorage)
                                            uiState.applyRecipe(it, freshRefs)
                                            originalImageUri = it.recipe.imageUri ?: ""
                                            lastSavedImagePath = originalImageUri
                                            isUnsavedImageChange = false
                                        }
                                        editingGuard.isEditing = false
                                    }
                                } else if (editingGuard.isEditing) {
                                    editingGuard.isEditing = false
                                } else {
                                    navController.navigateUp()
                                }
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!editingGuard.isEditing) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    val latest = viewModel.getRecipeWithIngredients(recipeId)
                                    val freshRefs = viewModel.ingredientRepository.getCrossRefsForRecipeOnce(recipeId)
                                    latest?.let {
                                        val restoredImageUri = it.recipe.imageUri.orEmpty()
                                        if (isUnsavedImageChange && uiState.imageUri != originalImageUri) {
                                            uiState.imageUri?.let { uri ->
                                                deleteImageFromStorage(uri, context)
                                            }
                                            viewModel.updateImageUri(originalImageUri)
                                            Log.d("imageCleanup", "🧹 Discarded unsaved image → ${uiState.imageUri}")
                                        }
                                        isUnsavedImageChange = false
                                        originalImageUri = restoredImageUri
                                        lastSavedImagePath = restoredImageUri
                                        uiState.applyRecipe(it, freshRefs)
                                        editingGuard.isEditing = true
                                    }
                                }
                            },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 📸 Image Picker Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
                    .clickable(enabled = editingGuard.isEditing) { pickImage() },
                contentAlignment = Alignment.Center
            ) {
                uiState.imageUri?.takeIf { it.isNotEmpty() }?.let { uriString ->
                    val uri = uriString.toUri()
                    Image(
                        painter = rememberAsyncImagePainter(model = uri),
                        contentDescription = "Recipe photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } ?: Text(
                    text = if (editingGuard.isEditing) "Tap to select image" else "No image",
                    color = Color.DarkGray
                )
            }

            // 🧾 Editable Form
            val hasChangesValue = hasChangesState
            val originalRecipe = recipeSnapshotState?.recipe

            RecipeDetailForm(
                uiState = uiState,
                isEditing = editingGuard.isEditing,
                pantryItems = pantryItems,
                onFieldChange = viewModel::updateUi, // New callback for field-level updates
                onIngredientsChange = viewModel::updateIngredients,
                onColorSelect = viewModel::updateCardColor,
                onSave = {
                    if (uiState.name.text.isBlank()) {
                        showNameRequiredDialog = true
                        return@RecipeDetailForm
                    }

                    val updated = originalRecipe?.let { uiState.toRecipeModel(it) }
                    if (updated == null) return@RecipeDetailForm

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
                            updatedRecipe = updated,
                            updatedIngredients = uiState.ingredients,
                            originalImageUri = originalImageUri,
                            context = context
                        )

                        originalImageUri = uiState.imageUri.orEmpty()
                        lastSavedImagePath = uiState.imageUri
                        isUnsavedImageChange = false
                        navController.popBackStack("recipes_main", inclusive = false)

                        editingGuard.isEditing = false
                    }
                },
                onCancel = {
                    coroutineScope.launch {
                        val latest = viewModel.getRecipeWithIngredients(recipeId)
                        val freshRefs =
                            viewModel.ingredientRepository.getCrossRefsForRecipeOnce(recipeId)
                        latest?.let {
                            uiState.rollbackImageIfNeeded(
                                originalImageUri,
                                context,
                                ::deleteImageFromStorage
                            )
                            uiState.applyRecipe(it, freshRefs)
                            originalImageUri = it.recipe.imageUri ?: ""
                            lastSavedImagePath = originalImageUri
                            isUnsavedImageChange = false
                        }
                        editingGuard.isEditing = false
                    }
                },
                hasChanges = hasChangesState,
                onRequestCreatePantryItem = { name ->
                    pantryViewModel.insertAndReturn(name)
                },
                onToggleShoppingStatus = { item ->
                    pantryViewModel.toggleShoppingStatus(item.id, context)
                }
            )

            // 🗂 Dialogs
            RecipeDialogs(
                showDeleteDialog = showDeleteDialog,
                onConfirmDelete = {
                    coroutineScope.launch {
                        val original = viewModel.getRecipeWithIngredients(recipeId)
                        original?.let {
                            viewModel.delete(it.recipe, context)
                            editingGuard.isEditing = false
                            showDeleteDialog = false
                            navController.navigateUp()
                        }
                    }
                },
                onDismissDelete = { showDeleteDialog = false },
                showDuplicateNameDialog = showDuplicateNameDialog,
                onDismissDuplicate = { showDuplicateNameDialog = false },
                showNameRequiredDialog = showNameRequiredDialog,
                onDismissMissingName = { showNameRequiredDialog = false }
            )
        }
    }
}