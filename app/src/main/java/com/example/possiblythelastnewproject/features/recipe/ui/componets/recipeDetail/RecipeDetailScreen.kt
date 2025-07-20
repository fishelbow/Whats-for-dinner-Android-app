package com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeDetail

import androidx.activity.compose.BackHandler
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
import com.example.possiblythelastnewproject.core.utils.imagePicker
import com.example.possiblythelastnewproject.features.pantry.ui.PantryViewModel
import com.example.possiblythelastnewproject.features.recipe.data.RecipeWithIngredients
import com.example.possiblythelastnewproject.features.recipe.ui.RecipesViewModel
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import com.example.possiblythelastnewproject.core.utils.deleteImageFromStorage
import com.example.possiblythelastnewproject.features.recipe.data.entities.RecipePantryItemCrossRef
import com.example.possiblythelastnewproject.features.recipe.ui.componets.LocalEditingGuard
import com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeDetail.Solid.RecipeDialogs

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

    // 🧭 Initial load
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

    // 🔄 Reactive snapshot
    val crossRefsState by produceState(initialValue = emptyList<RecipePantryItemCrossRef>(), initialized.value) {
        value = if (initialized.value) {
            viewModel.ingredientRepository.getCrossRefsForRecipeOnce(recipeId)
        } else emptyList()
    }

    val recipeSnapshotState by produceState<RecipeWithIngredients?>(initialValue = null, initialized.value) {
        value = if (initialized.value) {
            viewModel.getRecipeWithIngredients(recipeId)
        } else null
    }

    // 🧠 Change detection
    val hasChangesState by remember(uiState, isUnsavedImageChange, crossRefsState, recipeSnapshotState, originalImageUri) {
        derivedStateOf {
            val snapshot = recipeSnapshotState
            val crossRefs = crossRefsState
            val changed = snapshot?.let {
                uiState.isChangedFrom(it, crossRefs, originalImageUri)
            } ?: false
            changed || isUnsavedImageChange
        }
    }

    // 📸 Image picker
    val pickImage = imagePicker { selectedUri ->
        editingGuard.isEditing = true
        selectedUri?.toString()?.let { newPath ->
            val currentStoredImage = uiState.imageUri.orEmpty()
            if (currentStoredImage.isNotEmpty() && currentStoredImage != newPath) {
                deleteImageFromStorage(currentStoredImage, context)
            }
            viewModel.updateImageUri(newPath)
            lastSavedImagePath = newPath
            if (newPath != originalImageUri) {
                isUnsavedImageChange = true
            }
        }
    }

    // 🔙 Back navigation
    BackHandler(enabled = editingGuard.isEditing) {
        if (hasChangesState) {
            editingGuard.requestExit(
                rollback = performRecipeRollback(
                    recipeId,
                    context,
                    viewModel,
                    uiState,
                    originalImageUri,
                    setImagePath = { originalImageUri = it; lastSavedImagePath = it },
                    resetUnsavedFlag = { isUnsavedImageChange = false }
                ),
                thenExit = { editingGuard.isEditing = false }
            )
        } else {
            editingGuard.isEditing = false
        }
    }

    // 🧩 UI Scaffold
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recipe Details") },
                navigationIcon = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            val snapshot = recipeSnapshotState
                            val refs = crossRefsState
                            val hasChanges = snapshot?.let {
                                uiState.isChangedFrom(it, refs, originalImageUri)
                            } ?: false

                            if (editingGuard.isEditing && hasChanges) {
                                editingGuard.requestExit(
                                    rollback = {
                                        snapshot?.let {
                                            val restoredRecipe = uiState.rollbackFromSnapshot(
                                                snapshot = it,
                                                crossRefs = refs,
                                                originalImageUri = originalImageUri,
                                                context = context,
                                                deleteFn = ::deleteImageFromStorage,
                                                onImageReset = { uri ->
                                                    originalImageUri = uri
                                                    lastSavedImagePath = uri
                                                },
                                                resetUnsavedImageChange = {
                                                    isUnsavedImageChange = false
                                                }
                                            )
                                            viewModel.restoreRecipeState(restoredRecipe, uiState.ingredients)
                                        }
                                    },
                                    thenExit = { editingGuard.isEditing = false }
                                )
                            } else if (editingGuard.isEditing) {
                                editingGuard.isEditing = false
                            } else {
                                navController.navigateUp()
                            }
                        }
                    }) {
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
            // 📸 Image preview
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

            // 🧾 Form
            val hasChangesValue = hasChangesState
            val originalRecipe = recipeSnapshotState?.recipe

            RecipeDetailForm(
                uiState = uiState,
                isEditing = editingGuard.isEditing,
                pantryItems = pantryItems,
                onFieldChange = viewModel::updateUi,
                onIngredientsChange = viewModel::updateIngredients,
                onColorSelect = viewModel::updateCardColor,
                onSave = {
                    if (uiState.name.text.isBlank()) {
                        showNameRequiredDialog = true
                        return@RecipeDetailForm
                    }
                    val updated = originalRecipe?.let {
                        uiState.toRecipeModel(it)
                    } ?: return@RecipeDetailForm
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
                        editingGuard.isEditing = false
                        navController.popBackStack()
                    }
                },
                onCancel = { hasChanges, requestExit, exitCleanly ->
                    if (hasChanges && editingGuard.isEditing) {
                        coroutineScope.launch {
                            val snapshotRecipe = recipeSnapshotState
                            val snapshotRefs = crossRefsState

                            snapshotRecipe?.let {
                                val restoredRecipe = uiState.rollbackFromSnapshot(
                                    snapshot = it,
                                    crossRefs = snapshotRefs,
                                    originalImageUri = originalImageUri,
                                    context = context,
                                    deleteFn = ::deleteImageFromStorage,
                                    onImageReset = { uri ->
                                        originalImageUri = uri
                                        lastSavedImagePath = uri
                                    },
                                    resetUnsavedImageChange = {
                                        isUnsavedImageChange = false
                                    }
                                )
                                viewModel.restoreRecipeState(restoredRecipe, uiState.ingredients)
                                editingGuard.isEditing = false
                            }

                        }
                    } else {
                        editingGuard.isEditing = false
                    }
                },
                hasChanges = hasChangesValue,
                onRequestCreatePantryItem = { name -> pantryViewModel.insertAndReturn(name) },
                onToggleShoppingStatus = { item ->
                    pantryViewModel.toggleShoppingStatus(item.id, context)
                }
            )
        }

        // 🧹 Dialogs
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