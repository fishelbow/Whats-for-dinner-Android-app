package com.example.possiblythelastnewproject.features.scan.domain.scanTools;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

public class CustomCameraManager {
    private static final String TAG = "CustomCameraManager";

    private final Context         context;
    private final LifecycleOwner  lifecycleOwner;
    private final PreviewView     previewView;
    private final DataExtractor   dataExtractor;
    private final CameraScanCallback scanCallback;

    private ProcessCameraProvider cameraProvider;
    private ImageAnalysis         imageAnalysis;
    private volatile boolean      isScanningAllowed = true;

    public CustomCameraManager(
            @NonNull Context context,
            @NonNull LifecycleOwner lifecycleOwner,
            @NonNull PreviewView previewView,
            @NonNull DataExtractor dataExtractor,
            @NonNull CameraScanCallback scanCallback
    ) {
        this.context        = context;
        this.lifecycleOwner = lifecycleOwner;
        this.previewView    = previewView;
        this.dataExtractor  = dataExtractor;
        this.scanCallback   = scanCallback;
    }

    public void startCamera() {
        ProcessCameraProvider.getInstance(context)
                .addListener(() -> {
                            try {
                                cameraProvider = ProcessCameraProvider.getInstance(context).get();

                                // 1) Preview
                                Preview preview = new Preview.Builder().build();
                                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                                // 2) Analysis
                                imageAnalysis = new ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build();

                                // 3) Always close, either drop or process
                                imageAnalysis.setAnalyzer(
                                        ContextCompat.getMainExecutor(context),
                                        imageProxy -> {
                                            if (!isScanningAllowed) {
                                                // paused → drop frame & close
                                                imageProxy.close();
                                            } else {
                                                // allowed → pass to DataExtractor (it will close)
                                                dataExtractor.extractData(imageProxy, scanCallback);
                                            }
                                        }
                                );

                                // 4) bind to lifecycle
                                cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        new CameraSelector.Builder()
                                                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                                                .build(),
                                        preview,
                                        imageAnalysis
                                );

                                Log.d(TAG, "Camera started successfully");
                            } catch (Exception e) {
                                Log.e(TAG, "CameraX init failed", e);
                            }
                        },
                        ContextCompat.getMainExecutor(context));
    }

    public void stopCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            Log.d(TAG, "Camera stopped.");
        }
    }

    public void pauseScanning() {
        isScanningAllowed = false;
        Log.d(TAG, "Scanning paused.");
    }

    public void resumeScanning() {
        isScanningAllowed = true;
        Log.d(TAG, "Scanning resumed.");
    }
}