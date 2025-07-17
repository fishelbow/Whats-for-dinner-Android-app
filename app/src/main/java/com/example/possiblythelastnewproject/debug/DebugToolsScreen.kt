package com.example.possiblythelastnewproject.debug

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
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

    var pantryCount by remember { mutableFloatStateOf(1000f) }
    var recipeCount by remember { mutableFloatStateOf(200f) }

    val isLoading by viewModel.isLoading
    val progress by viewModel.progress

    if (isLoading) {
        BackHandler(enabled = true) {}
    }

    val launchImagePicker = imagePicker { pickedUri ->
        coroutineScope.launch {
            viewModel.loadTestData(
                context = context,
                imageUri = pickedUri,
                pantryCount = pantryCount.toInt(),
                recipeCount = recipeCount.toInt()
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 🎛 Top controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
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
                onClick = launchImagePicker,
                enabled = !isLoading
            ) {
                Text("Generate Test Data")
            }
        }

        // 🚨 Bottom persistent actions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter)
        ) {
            if (isLoading) {
                Text(
                    "Loading... ${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium
                )
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
                    onClick = { viewModel.clearAllData() },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    Text("Clear Test Data")
                }

                Button(
                    onClick = { viewModel.wipeDatabase(context) },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete DB")
                }
            }
        }
    }
}