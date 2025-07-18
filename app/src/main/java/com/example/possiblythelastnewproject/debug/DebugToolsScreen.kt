package com.example.possiblythelastnewproject.debug

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.possiblythelastnewproject.core.utils.imagePicker
import kotlinx.coroutines.launch

@Composable
fun DebugToolsScreen() {
    val context = LocalContext.current
    val viewModel: DebugViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()

    var pantryCount by remember { mutableFloatStateOf(0f) }
    var recipeCount by remember { mutableFloatStateOf(0f) }
    var ingredientAmount by remember { mutableFloatStateOf(0f) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSecondConfirm by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading
    val progress by viewModel.progress

    if (isLoading) BackHandler(enabled = true) {}

    val launchImagePicker = imagePicker { pickedUri ->
        coroutineScope.launch {
            viewModel.loadTestData(
                context = context,
                imageUri = pickedUri,
                pantryCount = pantryCount.toInt(),
                recipeCount = recipeCount.toInt(),
                ingredientCount = ingredientAmount.toInt()
            )
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 🧮 Scrollable sliders section
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            SliderWithLabel(
                label = "Avg Ingredients per Recipe",
                value = ingredientAmount,
                onValueChange = { ingredientAmount = it },
                valueRange = 1f..100f,
                enabled = !isLoading
            )

            SliderWithLabel(
                label = "Pantry Items",
                value = pantryCount,
                onValueChange = { pantryCount = it },
                valueRange = 0f..100_000f,
                enabled = !isLoading
            )

            SliderWithLabel(
                label = "Recipes",
                value = recipeCount,
                onValueChange = { recipeCount = it },
                valueRange = 0f..5_000f,
                enabled = !isLoading
            )
        }

        // ⚙️ Persistent bottom controls
        Column {
            if (isLoading) {
                Text("Loading... ${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = launchImagePicker,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    Text("Generate Data")
                }

                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete DB")
                }
            }
        }
    }

    //  First confirmation
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete the database? This action is irreversible.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    showSecondConfirm = true
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    //  Final confirmation
    if (showSecondConfirm) {
        AlertDialog(
            onDismissRequest = { showSecondConfirm = false },
            title = { Text("Final Confirmation") },
            text = { Text("Are you 100% sure? Everything will be wiped.") },
            confirmButton = {
                TextButton(onClick = {
                    showSecondConfirm = false
                    viewModel.wipeDatabase(context)
                }) {
                    Text("Yes, Wipe DB", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSecondConfirm = false }) {
                    Text("Go Back")
                }
            }
        )
    }
}

@Composable
fun SliderWithLabel(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 20,
    enabled: Boolean = true
) {
    Text("$label: ${value.toInt()}", style = MaterialTheme.typography.bodySmall)
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        steps = steps,
        enabled = enabled
    )
    Spacer(modifier = Modifier.height(8.dp))
}