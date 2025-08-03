package com.example.possiblythelastnewproject.debug

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.possiblythelastnewproject.core.utils.compressUriToInternalStorage
import com.example.possiblythelastnewproject.core.utils.imagePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@SuppressLint("ContextCastToActivity")
@Composable
fun DebugToolsScreen() {
    val context = LocalContext.current
    val viewModel: DebugViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()

    val scrollState = rememberScrollState()

    // UI state
    var pantryCount by viewModel.pantryCount
    var recipeCount by viewModel.recipeCount
    var ingredientAmount by viewModel.ingredientAmount

    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSecondConfirm by remember { mutableStateOf(false) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val isLoading by viewModel.isLoading
    val progress by viewModel.progress


    val activity = LocalContext.current as? Activity

    if (isLoading) BackHandler(enabled = true) {}

    // 🔧 Image Generators

    fun launchSyntheticGeneration() {
        val generator: suspend (String) -> Uri = { label ->
            withContext(Dispatchers.IO) {
                generateMockImage(context, label)
            }
        }

        viewModel.beginLoading()
        coroutineScope.launch {
            viewModel.loadTestData(
                context = context,
                pantryCount = pantryCount.toInt(),
                recipeCount = recipeCount.toInt(),
                ingredientCount = ingredientAmount.toInt(),
                generateImage = generator,
                keepScreenAwake = { activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) },
                releaseScreenAwake = { activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
            )
        }
    }

    fun launchClonedGeneration(source: Uri?) {
        if (source == null) return

        val generator: suspend (String) -> Uri = { label ->
            withContext(Dispatchers.IO) {
              //  val uuid = UUID.randomUUID().toString()

                // Now copy returns a Uri (not a String path)
                val copiedUri = compressUriToInternalStorage(context, source)

                copiedUri ?: generateMockImage(context, "fallback") //
            }
        }

        viewModel.beginLoading()
        coroutineScope.launch {
            viewModel.loadTestData(
                context = context,
                pantryCount = pantryCount.toInt(),
                recipeCount = recipeCount.toInt(),
                ingredientCount = ingredientAmount.toInt(),
                generateImage = generator
            )
        }
    }

    val pickFromGallery = imagePicker { uri ->
        selectedImageUri = uri
        showImageSourceDialog = false
        launchClonedGeneration(uri)
    }

    // 🧱 UI Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            SliderWithLabel(
                label = "Ingredients per Recipe",
                value = ingredientAmount,
                onValueChange = { ingredientAmount = it },
                scale = 1f,
                max = 100f,
                enabled = !isLoading
            )

            SliderWithLabel(
                label = "Pantry Items",
                value = pantryCount,
                onValueChange = { pantryCount = it },
                scale = 10f, // granularity of x items per step
                max = 500_000f,
                enabled = !isLoading
            )

            SliderWithLabel(
                label = "Recipes",
                value = recipeCount,
                onValueChange = { recipeCount = it },
                scale = 10f,
                max = 100_000f,
                enabled = !isLoading
            )
        }

        Column {
            if (isLoading) ProgressPanel(progress, viewModel.loadingStage, viewModel.loadingDetail)

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = { showImageSourceDialog = true }, modifier = Modifier.weight(1f), enabled = !isLoading) {
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

    // 🔐 Dialogs
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Choose Image Source") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Select how mock images should be created:")
                    Button(onClick = { showImageSourceDialog = false; pickFromGallery() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Camera or File")
                    }
                    Button(onClick = { showImageSourceDialog = false; launchSyntheticGeneration() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Generate Automatically")
                    }
                }
            },
            confirmButton = {
                Box(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = { showImageSourceDialog = false },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) { Text("Cancel") }
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete the database? This action is irreversible.") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; showSecondConfirm = true }) {
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

    if (showSecondConfirm) {
        AlertDialog(
            onDismissRequest = { showSecondConfirm = false },
            title = { Text("Final Confirmation") },
            text = { Text("Are you 100% sure? Everything will be wiped.") },
            confirmButton = {
                TextButton(onClick = { showSecondConfirm = false; viewModel.wipeDatabase(context) }) {
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
    scale: Float,
    max: Float,
    enabled: Boolean
) {
    val steps = (max / scale).toInt().coerceAtLeast(0)
    val normalizedValue = value / max

    val displayValue = remember(value) { value.toInt() }

    Text("$label: $displayValue", style = MaterialTheme.typography.bodySmall)

    Slider(
        value = normalizedValue,
        onValueChange = { onValueChange(it * max) },
        valueRange = 0f..1f,
        steps = steps,
        enabled = enabled
    )

    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun ProgressPanel(
    progress: Float,
    stageFlow: State<String>,
    detailFlow: State<String>
) {
    val stage by stageFlow
    val detail by detailFlow
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text("$stage ${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
        if (detail.isNotBlank()) {
            Text(detail, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp))
        }
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
    }
}