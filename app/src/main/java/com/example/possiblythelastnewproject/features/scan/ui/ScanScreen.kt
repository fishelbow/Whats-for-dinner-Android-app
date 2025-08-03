package com.example.possiblythelastnewproject.features.scan.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.possiblythelastnewproject.BuildConfig
import com.example.possiblythelastnewproject.backup.BackupViewModel
import com.example.possiblythelastnewproject.debug.DebugToolsScreen
import com.example.possiblythelastnewproject.features.scan.domain.scanTools.CameraScanCallback
import com.example.possiblythelastnewproject.features.scan.domain.scanTools.CustomCameraManager
import com.example.possiblythelastnewproject.features.scan.domain.scanTools.DataExtractor
import com.example.possiblythelastnewproject.backup.DbImportExportDialog
import kotlinx.coroutines.delay

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun ScanningTab(
    shouldScan: Boolean,
    onScanResult: (String) -> Unit
) {

    val viewModel: BackupViewModel = hiltViewModel()
    val result = viewModel.result
    var showDialog by remember { mutableStateOf(false) }

    val ctx = LocalContext.current
    val activity = remember(ctx) {
        (ctx as? ComponentActivity)
            ?: error("ScanningTab must be hosted in a ComponentActivity")
    }
    val lifecycleOwner = LocalLifecycleOwner.current

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> uri?.let { viewModel.handleImportZip(it) } }
    )

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
        onResult = { uri -> uri?.let { viewModel.handleExportZip(it) } }
    )



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

    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp

    val previewHeightTarget = if (isPaused) {
        screenWidthDp * 0.33f // Shrinks to ~⅓ of screen width when paused
    } else {
        screenWidthDp * 0.75f // Expands to ~¾ of screen width when active
    }

    val previewHeight by animateDpAsState(
        targetValue = previewHeightTarget,
        label = "PreviewHeight"
    )

    // Automatically pause/resume scanning from shouldScan
    LaunchedEffect(shouldScan) {
        cameraManager?.let {
            if (shouldScan) it.resumeScanning()
            else it.pauseScanning()
        }
    }

    // Pulse effect when scan resumes
    var showPulse by remember { mutableStateOf(false) }
    LaunchedEffect(shouldScan) {
        if (shouldScan) {
            showPulse = true
            delay(500)
            showPulse = false
        }
    }

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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(previewHeight) // ✅ dynamic height here
                    .clip(RoundedCornerShape(8.dp))
                    .border(stroke, Color.White, RoundedCornerShape(8.dp))
            ) {
                val innerMod = Modifier.matchParentSize().padding(stroke)

                AndroidView(
                    modifier = innerMod,
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                                override fun onViewAttachedToWindow(v: View) {
                                    cameraManager = CustomCameraManager(
                                        activity, lifecycleOwner, this@apply,
                                        DataExtractor(),
                                        object : CameraScanCallback {
                                            override fun onScanResult(data: String) = onScanResult(data)
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
                        Modifier.align(Alignment.Center)
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(previewHeight) // ✅ dynamic height now respected
                .border(stroke, Color.White, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(stroke),
                factory = { ctx ->
                    val density = ctx.resources.displayMetrics.density
                    val previewHeightPx = (previewHeight.value * density).toInt()

                    PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            previewHeightPx // 👈 forces native view to follow Compose height
                        )

                        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                            override fun onViewAttachedToWindow(v: View) {
                                cameraManager = CustomCameraManager(
                                    activity,
                                    lifecycleOwner,
                                    this@apply,
                                    DataExtractor(),
                                    object : CameraScanCallback {
                                        override fun onScanResult(data: String) = onScanResult(data)
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
                Text("⚙️ Import/Export")
            }
            if (BuildConfig.DEBUG) { // <- one day I will know how to work this lol.
                DebugToolsScreen()
            }

            DbImportExportDialog(
                showDialog = showDialog,
                isLoading = viewModel.isLoading,
                progress = viewModel.progress,
                statusMessage = viewModel.statusMessage,
                onDismiss = {
                    showDialog = false
                    viewModel.clearResult()
                },
                onImportClick = { importLauncher.launch(arrayOf("application/zip")) },
                onExportClick = { exportLauncher.launch("backup.zip") }
            )

            LaunchedEffect(result) {
                result?.let {
                    Toast.makeText(ctx, it, Toast.LENGTH_LONG).show()
                    viewModel.clearResult()
                }
            }
        }

        // this disposable effect to keep the screen from timing out during long loads

        DisposableEffect(isPaused) {
            val window = activity.window
            if (isPaused) {
                window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            onDispose {
                window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }
}
