package com.example.possiblythelastnewproject.core.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

@Composable
fun imagePicker(
    onImagePicked: (Uri?) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            coroutineScope.launch {
                val savedUri = copyUriToInternalStorage(context, it)
                onImagePicked(savedUri)
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            coroutineScope.launch {
                val savedUri = saveBitmapToInternalStorage(context, it)
                onImagePicked(savedUri)
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(null)
        } else {
            // Optionally notify the user that camera access is required
        }
    }

    if (showDialog) {
        ImageSourceDialog(
            onDismiss = { showDialog = false },
            onPickGallery = {
                galleryLauncher.launch(arrayOf("image/*"))
                showDialog = false
            },
            onTakePhoto = {
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

    return remember { { showDialog = true } }
}

fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): Uri? {
    val filename = "camera_${System.currentTimeMillis()}.jpg"
    return try {
        val file = File(context.filesDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        }
        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun copyUriToInternalStorage(context: Context, sourceUri: Uri): Uri? {
    return try {
        val inputStream = context.contentResolver.openInputStream(sourceUri)
        val filename = "gallery_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, filename)
        FileOutputStream(file).use { out ->
            inputStream?.copyTo(out)
        }
        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
fun deleteInternalImage(context: Context, uri: Uri?) {
    uri?.let {
        try {
            val file = File(it.path ?: return)
            if (file.exists() && file.parent == context.filesDir.path) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


