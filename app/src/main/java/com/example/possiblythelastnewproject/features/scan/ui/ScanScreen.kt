package com.example.possiblythelastnewproject.features.scan.ui

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.possiblythelastnewproject.features.scan.domain.scanTools.CameraScanCallback
import com.example.possiblythelastnewproject.features.scan.domain.scanTools.CustomCameraManager
import com.example.possiblythelastnewproject.features.scan.domain.scanTools.DataExtractor

@Composable
fun ScanningTab(onScanResult: (String) -> Unit) {
    val ctx = LocalContext.current
    val activity = remember(ctx) {
        (ctx as? ComponentActivity)
            ?: error("ScanningTab must be hosted in a ComponentActivity")
    }
    val lifecycleOwner = LocalLifecycleOwner.current

    // --- PERMISSION LOGIC ---
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                activity, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }
    LaunchedEffect(Unit) {
        if (!hasPermission) launcher.launch(Manifest.permission.CAMERA)
    }
    if (!hasPermission) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Camera permission is required to scan.")
                Spacer(Modifier.height(8.dp))
                Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant Permission")
                }
            }
        }
        return
    }

    // --- SHARED STATE ---
    var cameraManager by remember { mutableStateOf<CustomCameraManager?>(null) }
    var isPaused by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        onDispose {
            cameraManager?.stopCamera()
            cameraManager = null
        }
    }

    // stroke width for border + padding to inset the PreviewView
    val stroke = 2.dp

    // --- PREVIEW MODE ONLY ---
    if (LocalInspectionMode.current) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Black box + white border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .border(stroke, Color.White, RoundedCornerShape(8.dp))
                    .background(Color.Black)
            )
            Spacer(Modifier.height(16.dp))
            // Toggle button
            var previewPaused by remember { mutableStateOf(false) }
            Button(onClick = { previewPaused = !previewPaused }) {
                Text(if (previewPaused) "Resume Scanning" else "Pause Scanning")
            }
        }
        return
    }

    // --- RUNTIME LAYOUT ---
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1) Camera container: border + rounded corners
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .border(stroke, Color.White, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
        ) {
            // 2) inset by stroke so PreviewView sits *inside* the border
            val innerMod = Modifier
                .matchParentSize()
                .padding(stroke)

            AndroidView(
                modifier = innerMod,
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        addOnAttachStateChangeListener(object :
                            View.OnAttachStateChangeListener {
                            override fun onViewAttachedToWindow(v: View) {
                                cameraManager = CustomCameraManager(
                                    activity,
                                    lifecycleOwner,
                                    this@apply,
                                    DataExtractor(),
                                    object :
                                        CameraScanCallback {
                                        override fun onScanResult(data: String) =
                                            onScanResult(data)

                                        override fun onScanError(e: Exception) {
                                            Log.e("ScanningTab", "scan error: ${e.message}", e)
                                        }
                                    }
                                ).also { it.startCamera() }
                                removeOnAttachStateChangeListener(this)
                            }
                            override fun onViewDetachedFromWindow(v: View) = Unit
                        })
                    }
                }
            )
        }

        Spacer(Modifier.height(16.dp))

        // 3) Pause/Resume button
        Button(onClick = {
            if (isPaused) cameraManager?.resumeScanning()
            else           cameraManager?.pauseScanning()
            isPaused = !isPaused
        }) {
            Text(if (isPaused) "Resume Scanning" else "Pause Scanning")
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun ScanningTabPreview() {
    ScanningTab(onScanResult = {})
}