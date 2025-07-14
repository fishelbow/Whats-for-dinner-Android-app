package com.example.possiblythelastnewproject.debug

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.possiblythelastnewproject.core.utils.imagePicker
import kotlinx.coroutines.launch

@Composable
fun DebugToolsScreen() {
    val viewModel: DebugViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()

    var pantryCount by remember { mutableStateOf(1000f) }
    var recipeCount by remember { mutableStateOf(200f) }

    val isLoading by viewModel.isLoading
    val progress by viewModel.progress

    if (isLoading) {
        BackHandler(enabled = true) {}
    }

    val launchImagePicker = imagePicker { imageBytes ->
        coroutineScope.launch {
            viewModel.loadTestData(
                imageBytes = imageBytes,
                pantryCount = pantryCount.toInt(),
                recipeCount = recipeCount.toInt()
            )
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Pantry Items: ${pantryCount.toInt()}")
        Slider(
            value = pantryCount,
            onValueChange = { pantryCount = it },
            valueRange = 100f..5000f,
            steps = 9,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Recipes: ${recipeCount.toInt()}")
        Slider(
            value = recipeCount,
            onValueChange = { recipeCount = it },
            valueRange = 50f..1000f,
            steps = 9,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = launchImagePicker, enabled = !isLoading
        ) {
            Text("Generate Test Data with Image")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { viewModel.clearAllData() }, enabled = !isLoading
        ) {
            Text("Clear All Test Data")
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Loading... ${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )
        }
    }
}