package com.example.possiblythelastnewproject.features.pantry.ui.pantryScreen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.possiblythelastnewproject.core.utils.deleteImageFromStorage
import com.example.possiblythelastnewproject.core.utils.imagePicker
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.pantry.ui.PantryViewModel
import com.example.possiblythelastnewproject.features.pantry.ui.pantryScreen.pantryDialogs.AddIngredientDialog
import com.example.possiblythelastnewproject.features.pantry.ui.pantryScreen.pantryDialogs.DeleteConfirmationDialog
import com.example.possiblythelastnewproject.features.pantry.ui.pantryScreen.pantryDialogs.DuplicateCodeDialog
import com.example.possiblythelastnewproject.features.pantry.ui.pantryScreen.pantryDialogs.EditIngredientDialog
import com.example.possiblythelastnewproject.features.pantry.ui.pantryScreen.pantryDialogs.IngredientDetailsDialog
import com.example.possiblythelastnewproject.features.pantry.ui.pantryScreen.pantryDialogs.ScanDialog

@Composable
fun PantryScreen(viewModel: PantryViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val pantryItems by viewModel.pantryItems.collectAsState()
    val inUseIds by viewModel.inUsePantryItemIds.collectAsState(emptySet())
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    var selectedItem by remember { mutableStateOf<PantryItem?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showScanDialog by remember { mutableStateOf(false) }
    var showDuplicateCodeDialog by remember { mutableStateOf(false) }
    var showDuplicateNameDialog by remember { mutableStateOf(false) }
    var showBlankNameDialog by remember { mutableStateOf(false) }
    var newIngredient by remember { mutableStateOf("") }

    val launchImagePickerForAdd = imagePicker { uri ->
        uri?.let { viewModel.swapAddImage(context, it) }
    }

    val launchImagePickerForEdit = imagePicker { uri ->
        uri?.let { viewModel.swapEditImage(context, it) }
    }

    val filteredItems = pantryItems.filter {
        uiState.searchQuery.isBlank() || it.name.contains(uiState.searchQuery, ignoreCase = true)
    }

    LaunchedEffect(showAddDialog) {
        if (showAddDialog) viewModel.clearAddImageUri()
    }

    Scaffold(
        topBar = {
            IngredientSearchBar(
                searchQuery = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onAddNewIngredient = { showAddDialog = true },
                focusManager = focusManager
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            PantryGridSection(
                filteredItems = filteredItems,
                onItemClick = { selectedItem = it }
            )
        }
    }

    selectedItem?.let {
        IngredientDetailsDialog(
            item = it,
            onEdit = {
                viewModel.startEditing(it)
                selectedItem = null
            },
            onDismiss = { selectedItem = null },
            onScanClick = { showScanDialog = true }
        )
    }

    if (showAddDialog) {
        AddIngredientDialog(
            newIngredient = newIngredient,
            onNameChange = { newIngredient = it },
            onDismiss = {
                viewModel.uiState.value.addImageUri?.let {
                    deleteImageFromStorage(it, context)
                }
                viewModel.clearAddImageUri()
                viewModel.updateSelectedCategory(null)
                newIngredient = ""
                showAddDialog = false
            },
            onConfirm = {
                val trimmedName = newIngredient.trim()
                val nameExists = pantryItems.any {
                    it.name.equals(trimmedName, ignoreCase = true)
                }

                if (trimmedName.isNotBlank() && !nameExists) {
                    val finalCategory = uiState.selectedCategory
                        ?: viewModel.allCategories.value.firstOrNull {
                            it.name.equals("Other", ignoreCase = true)
                        }

                    viewModel.addPantryItem(
                        PantryItem(
                            name = trimmedName,
                            quantity = 1,
                            imageUri = uiState.addImageUri,
                            category = finalCategory?.name ?: "Other"
                        )
                    )
                    newIngredient = ""
                    showAddDialog = false
                    viewModel.updateSelectedCategory(null)
                }
            },
            launchImagePicker = launchImagePickerForAdd,
            categories = viewModel.allCategories.collectAsState(),
            selectedCategory = uiState.selectedCategory,
            onCategorySelect = viewModel::updateSelectedCategory,
            nameExists = pantryItems.any { it.name.equals(newIngredient.trim(), ignoreCase = true) }
        )
    }

    EditIngredientDialog(
        uiState = uiState,
        pantryItems = pantryItems,
        inUseIds = inUseIds,
        viewModel = viewModel,
        context = context,
        launchImagePicker = launchImagePickerForEdit,
        showDuplicateNameDialog = showDuplicateNameDialog,
        onDuplicateNameDialogDismiss = { showDuplicateNameDialog = false },
        showBlankNameDialog = showBlankNameDialog,
        onBlankNameDialogDismiss = { showBlankNameDialog = false },
        onShowDuplicateNameDialog = { showDuplicateNameDialog = true },
        onShowBlankNameDialog = { showBlankNameDialog = true }
    )

    DeleteConfirmationDialog(
        itemToDelete = uiState.itemToDelete,
        inUseIds = inUseIds,
        onCancel = { viewModel.cancelDelete() },
        onConfirmDelete = { viewModel.confirmDelete(context) }
    )

    ScanDialog(
        showScanDialog = showScanDialog,
        onDismiss = { showScanDialog = false },
        pantryItems = pantryItems,
        selectedItem = selectedItem,
        onScanSuccess = { id, code ->
            viewModel.updateScanCode(id, code, context)
            showScanDialog = false
        },
        onDuplicateScan = {
            showDuplicateCodeDialog = true
            showScanDialog = false
        },
        onItemUpdate = { updated: PantryItem -> selectedItem = updated }
    )

    DuplicateCodeDialog(
        visible = showDuplicateCodeDialog,
        onDismiss = { showDuplicateCodeDialog = false }
    )
}