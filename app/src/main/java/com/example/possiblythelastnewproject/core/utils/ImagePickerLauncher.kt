package com.example.possiblythelastnewproject.core.utils

import android.Manifest
import android.graphics.Bitmap
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

@Composable
fun imagePicker(
    onImagePicked: (ByteArray) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Processing state flag (optional: you can expose this to drive a loading UI)
    var isProcessing by remember { mutableStateOf(false) }

    // Launcher for picking an image from the gallery.
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                isProcessing = true
                // Process on the IO dispatcher to avoid main-thread work:
                val bytes = withContext(Dispatchers.IO) {
                    compressImageFromUri(context, it, 300, 300, 80)
                }
                bytes?.let { processed ->
                    onImagePicked(processed)
                }
                isProcessing = false
            }
        }
    }

    // Launcher for taking a photo (returns a Bitmap).
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            coroutineScope.launch {
                isProcessing = true
                val stream = ByteArrayOutputStream()
                // Compression can be done off the main thread if needed:
                withContext(Dispatchers.IO) {
                    it.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                }
                onImagePicked(stream.toByteArray())
                isProcessing = false
            }
        }
    }

    // Launcher for requesting CAMERA permission.
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(null)
        } else {
            // Optionally, inform the user that permission is required.
        }
    }

    // Local state to control showing the source selection dialog.
    var showDialog by remember { mutableStateOf(false) }

    // If the dialog is open, show the image source selection dialog.
    if (showDialog) {
        ImageSourceDialog(
            onDismiss = { showDialog = false },
            onPickGallery = {
                galleryLauncher.launch("image/*")
                showDialog = false
            },
            onTakePhoto = {
                // Check for CAMERA permission before launching:
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    cameraLauncher.launch(null)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
                showDialog = false
            }
        )
    }

    // Return a lambda that triggers the dialog.
    // You can also consider exposing the `isProcessing` value to show a loading indicator.
    return remember { { showDialog = true } }
}