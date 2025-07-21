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
import com.example.possiblythelastnewproject.features.recipe.data.entities.RecipePantryItemCrossRef
import com.example.possiblythelastnewproject.features.recipe.ui.componets.LocalEditingGuard
import com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeDetail.RecipeDetailComponets.RecipeDetailForm
import com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeDetail.RecipeDetailComponets.RecipeDialogs

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
    val context = LocalContext.current
    val editingGuard = LocalEditingGuard.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDuplicateNameDialog by remember { mutableStateOf(false) }
    var showNameRequiredDialog by remember { mutableStateOf(false) }

    // 🧭 Initial load
    LaunchedEffect(recipeId) {
        viewModel.loadRecipe(recipeId)
        editingGuard.isEditing = false
        viewModel.activeRecipeId = recipeId
    }

    // 🧠 Derived change detection
    val crossRefs by produceState(initialValue = emptyList<RecipePantryItemCrossRef>(), recipeId) {
        value = viewModel.ingredientRepository.getCrossRefsForRecipeOnce(recipeId)
    }
    val recipeSnapshot by produceState<RecipeWithIngredients?>(initialValue = null, recipeId) {
        value = viewModel.getRecipeWithIngredients(recipeId)
    }
    val hasChanges by remember(uiState, crossRefs, recipeSnapshot) {
        derivedStateOf {
            recipeSnapshot?.let { uiState.hasAnyChangesComparedTo(it, crossRefs) } ?: false
        }
    }

    // 🛡️ Sync guard lifecycle to image state
    LaunchedEffect(uiState.pendingImageUri) {
        val pending = uiState.pendingImageUri
        val committed = uiState.imageUri
        val hasPendingChange = !pending.isNullOrBlank() && pending != committed
        editingGuard.isEditing = hasPendingChange
    }

    // 📸 Image picker
    val pickImage = imagePicker { uri ->
        uri?.toString()?.let {
            viewModel.updatePendingImageUri(it)
            editingGuard.isEditing = true
        }
    }

    val backHandlerKey = "back_${recipeId}_${editingGuard.isEditing}_${System.currentTimeMillis()}"

    // 🔙 Back navigation
    key(backHandlerKey) {
        BackHandler(enabled = editingGuard.isEditing) {
            editingGuard.guardedExit(
                hasChanges = editingGuard.isEditing,
                rollback = { performRecipeRollback(recipeId, context, viewModel).invoke() },
                thenExit = {
                    editingGuard.isEditing = false
                    navController.popBackStack()
                },
                cleanExit = {
                    editingGuard.isEditing = false
                    navController.popBackStack()
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recipe Details") },
                navigationIcon = {
                    IconButton(onClick = {
                        editingGuard.guardedExit(
                            hasChanges = hasChanges,
                            rollback = performRecipeRollback(recipeId, context, viewModel),
                            thenExit = { editingGuard.isEditing = false },
                            cleanExit = { navController.navigateUp() }
                        )
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!editingGuard.isEditing) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                        TextButton(onClick = { editingGuard.isEditing = true }) {
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
            // 📷 Image preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
                    .clickable(enabled = editingGuard.isEditing) { pickImage() },
                contentAlignment = Alignment.Center
            ) {
                val displayUri = uiState.currentDisplayUri
                if (!displayUri.isNullOrBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(model = displayUri.toUri()),
                        contentDescription = "Recipe photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = if (editingGuard.isEditing) "Tap to select image" else "No image",
                        color = Color.DarkGray
                    )
                }
            }

            // 🧾 Form
            val originalRecipe = recipeSnapshot?.recipe
            RecipeDetailForm(
                uiState = uiState,
                isEditing = editingGuard.isEditing,
                pantryItems = pantryItems,
                onFieldChange = {
                    editingGuard.isEditing = true
                    viewModel.updateUi(it)
                },
                onIngredientsChange = {
                    editingGuard.isEditing = true
                    viewModel.updateIngredients(it)
                },
                onColorSelect = {
                    editingGuard.isEditing = true
                    viewModel.updateCardColor(it)
                },
                onRequestCreatePantryItem = { pantryViewModel.insertAndReturn(it) },
                onToggleShoppingStatus = { pantryViewModel.toggleShoppingStatus(it.id, context) },
                onSave = {
                    if (uiState.name.text.isBlank()) {
                        showNameRequiredDialog = true
                        return@RecipeDetailForm
                    }
                    val committedState = uiState.commitImage()
                    val updated = originalRecipe?.let { committedState.toRecipeModel(it) }
                    coroutineScope.launch {
                        if (updated != null) {
                            if (viewModel.recipeNameExists(updated.name, updated.uuid)) {
                                showDuplicateNameDialog = true
                                return@launch
                            }
                        }
                        if (updated != null) {
                            viewModel.updateRecipeWithIngredientsUi(
                                updatedRecipe = updated,
                                updatedIngredients = uiState.ingredients,
                                context = context
                            )
                        }
                        viewModel.commitImageUri()
                        editingGuard.isEditing = false
                        navController.popBackStack()
                    }
                },
                onCancel = {
                    editingGuard.guardedExit(
                        hasChanges = hasChanges,
                        rollback = performRecipeRollback(recipeId, context, viewModel),
                        thenExit = { editingGuard.isEditing = false },
                        cleanExit = { editingGuard.isEditing = false }
                    )
                },
                hasChanges = hasChanges
            )
        }
    }

    // 🧹 Dialogs
    RecipeDialogs(
        showDeleteDialog = showDeleteDialog,
        onConfirmDelete = {
            coroutineScope.launch {
                val original = viewModel.getRecipeWithIngredients(recipeId)
                original?.let {
                    viewModel.deleteRecipe(it.recipe, context)
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