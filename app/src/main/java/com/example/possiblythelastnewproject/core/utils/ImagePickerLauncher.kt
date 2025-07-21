package com.example.possiblythelastnewproject.core.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.media.ExifInterface
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream


// need to handle image rotation also

fun rotateBitmapIfRequired(context: Context, bitmap: Bitmap, sourceUri: Uri): Bitmap {
    return try {
        val input = context.contentResolver.openInputStream(sourceUri)
        val exif = input?.use { ExifInterface(it) }
        val orientation = exif?.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        val angle = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        val matrix = Matrix().apply {
            if (angle != 0f) postRotate(angle)

            // Optional: scale down large images for better preview cropping
            val maxPreviewDim = 1024
            val scaleX = if (bitmap.width > maxPreviewDim) maxPreviewDim.toFloat() / bitmap.width else 1f
            val scaleY = if (bitmap.height > maxPreviewDim) maxPreviewDim.toFloat() / bitmap.height else 1f
            val scale = minOf(scaleX, scaleY)
            if (scale < 1f) postScale(scale, scale)
        }

        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } catch (e: Exception) {
        Log.e("ImageCleanup", "⚠️ Rotation/Scaling failed → ${e.message}")
        bitmap
    }
}

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
                val savedUri = compressUriToInternalStorage(context, it)
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
            // Notify user if needed
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
    val tag = "ImageCleanup"
    val filename = "gallery_${System.currentTimeMillis()}.jpg"
    val file = File(context.filesDir, filename)

    return try {
        Log.d(tag, "📸 Saving camera bitmap → ${file.absolutePath}")
        FileOutputStream(file).use { out ->
            val success = bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            Log.d(tag, "🧼 Bitmap compression success → $success")
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        Log.d(tag, "✅ Image saved and URI created → $uri")
        uri
    } catch (e: Exception) {
        Log.e(tag, "🔥 Error saving camera image → ${e.message}", e)
        null
    }
}

fun compressUriToInternalStorage(context: Context, sourceUri: Uri): Uri? {
    val tag = "ImageCleanup"
    val filename = "gallery_${System.currentTimeMillis()}.jpg"
    val file = File(context.filesDir, filename)

    return try {
        Log.d(tag, "🖼 Compressing selected image → $sourceUri")

        context.contentResolver.openInputStream(sourceUri).use { inputStream ->
            if (inputStream == null) {
                Log.w(tag, "🚫 Input stream null for URI → $sourceUri")
                return null
            }

            val rawBitmap = BitmapFactory.decodeStream(inputStream)
            val rotatedBitmap = rotateBitmapIfRequired(context, rawBitmap, sourceUri)

            Log.d(tag, "🎨 Bitmap rotated → width: ${rotatedBitmap.width}, height: ${rotatedBitmap.height}")

            FileOutputStream(file).use { out ->
                val success = rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                Log.d(tag, "🧼 Bitmap compression success → $success")
            }
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        Log.d(tag, "✅ Compressed image saved → $uri")
        uri
    } catch (e: Exception) {
        Log.e(tag, "🔥 Error compressing and saving image → ${e.message}", e)
        null
    }
}