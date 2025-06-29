package com.example.possiblythelastnewproject.features.scan.ui

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.possiblythelastnewproject.core.data.sandbox.DbBackupViewModel
import com.example.possiblythelastnewproject.core.data.sandbox.rememberDbBackupLaunchers
import com.example.possiblythelastnewproject.features.scan.domain.scanTools.CameraScanCallback
import com.example.possiblythelastnewproject.features.scan.domain.scanTools.CustomCameraManager
import com.example.possiblythelastnewproject.features.scan.domain.scanTools.DataExtractor
import com.example.possiblythelastnewproject.features.scan.ui.componets.DbImportExportDialog
import kotlinx.coroutines.delay

@Composable
fun ScanningTab(
    shouldScan: Boolean,
    onScanResult: (String) -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) {
        (context as? ComponentActivity)
            ?: error("ScanningTab must be hosted in a ComponentActivity")
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel: DbBackupViewModel = hiltViewModel()
    val (launchImport, launchExport) = rememberDbBackupLaunchers(viewModel)
    var showDialog by remember { mutableStateOf(false) }


    var cameraManager by remember { mutableStateOf<CustomCameraManager?>(null) }
    var isPaused by remember { mutableStateOf(false) }
    var showPulse by remember { mutableStateOf(false) }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                activity, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
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
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant Permission")
                }
            }
        }
        return
    }

    LaunchedEffect(viewModel.result) {
        viewModel.result?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearResult()
        }
    }


    DisposableEffect(Unit) {
        onDispose {
            cameraManager?.stopCamera()
            cameraManager = null
        }
    }

    LaunchedEffect(shouldScan) {
        cameraManager?.let {
            if (shouldScan) it.resumeScanning()
            else it.pauseScanning()
        }
    }

    LaunchedEffect(shouldScan) {
        if (shouldScan) {
            showPulse = true
            delay(500)
            showPulse = false
        }
    }

    val stroke = 2.dp

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .border(stroke, Color.White, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
        ) {
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
                                    object : CameraScanCallback {
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

            if (showPulse) {
                Box(
                    Modifier
                        .align(Alignment.Center)
                        .size(100.dp)
                        .graphicsLayer {
                            scaleX = 1.2f
                            scaleY = 1.2f
                            alpha = 0.6f
                        }
                        .background(Color.Green.copy(alpha = 0.5f), shape = CircleShape)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            if (isPaused) cameraManager?.resumeScanning()
            else cameraManager?.pauseScanning()
            isPaused = !isPaused
        }) {
            Text(if (isPaused) "Resume Scanning" else "Pause Scanning")
        }

        if (isPaused) {
            Spacer(Modifier.height(8.dp))
            Button(onClick = { showDialog = true }) {
                Text("⚙️ Debug Tools")
            }
        }
    }

//spot for import/export dialog


}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun ScanningTabPreview() {
    ScanningTab(shouldScan = true, onScanResult = {})
}