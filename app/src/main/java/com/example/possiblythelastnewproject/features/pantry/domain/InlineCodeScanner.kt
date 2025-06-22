package com.example.possiblythelastnewproject.features.pantry.domain

import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.possiblythelastnewproject.features.scan.domain.scanTools.CameraScanCallback
import com.example.possiblythelastnewproject.features.scan.domain.scanTools.CustomCameraManager
import com.example.possiblythelastnewproject.features.scan.domain.scanTools.DataExtractor

@Composable
fun InlineBarcodeScanner(
    onResult: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val hasPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission.value = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission.value) {
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    if (!hasPermission.value) {
        Text("Camera permission required")
        return
    }

    var cameraManager by remember { mutableStateOf<CustomCameraManager?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            cameraManager?.stopCamera()
            cameraManager = null
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PreviewView(ctx).apply {
                val manager = CustomCameraManager(
                    context,
                    lifecycleOwner,
                    this,
                    DataExtractor(),
                    object : CameraScanCallback {
                        override fun onScanResult(data: String) {
                            onResult(data)
                        }

                        override fun onScanError(e: Exception) {
                            Log.e("InlineBarcodeScanner", "Error: ${e.message}", e)
                        }
                    }
                )
                manager.startCamera()
                cameraManager = manager
            }
        }
    )
}