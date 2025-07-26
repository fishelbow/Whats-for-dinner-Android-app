package com.example.possiblythelastnewproject.features.scan.domain.scanTools;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.util.Log;

import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.LinkedList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataExtractor {
    private static final String TAG = "DataExtractor";

    private final BarcodeScanner barcodeScanner;
    private final TextRecognizer textRecognizer;
    private final ToneGenerator toneGenerator;

    private final LinkedList<String> lastResults = new LinkedList<>();
    private long lastScanTime = 0;
    private static final long SCAN_DELAY_MS = 300;
    private long lastConfirmedScanTime = 0;
    private static final long CONFIRMED_SCAN_COOLDOWN_MS = 500;

    public DataExtractor() {
        barcodeScanner = BarcodeScanning.getClient(new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build());

        textRecognizer = TextRecognition.getClient(new TextRecognizerOptions.Builder().build());
        toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 150); //TODO MOVE THIS BACK TO 150 also provide some way to adjust this in app
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    public void extractData(ImageProxy imageProxy, final CameraScanCallback callback) {
        long currentTime = System.currentTimeMillis();

        if (!shouldProcessScan(currentTime)) {
            imageProxy.close();
            return;
        }

        InputImage image = convertImage(imageProxy);
        if (image == null) {
            callback.onScanError(new IllegalArgumentException("Invalid image provided."));
            return;
        }

        processBarcode(image, imageProxy, callback);
    }

    private boolean shouldProcessScan(long currentTime) {
        if (currentTime - lastScanTime < SCAN_DELAY_MS) {
            Log.i(TAG, "Skipping scan due to rate limit.");
            return false;
        }
        lastScanTime = currentTime;

        if (currentTime - lastConfirmedScanTime < CONFIRMED_SCAN_COOLDOWN_MS) {
            Log.i(TAG, "In confirmed scan cooldown, ignoring image.");
            return false;
        }
        return true;
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private InputImage convertImage(ImageProxy imageProxy) {
        try {
            return InputImage.fromMediaImage(Objects.requireNonNull(imageProxy.getImage()), imageProxy.getImageInfo().getRotationDegrees());
        } catch (Exception e) {
            Log.e(TAG, "Error converting ImageProxy: " + e.getMessage());
            imageProxy.close();
            return null;
        }
    }

    private void processBarcode(InputImage image, ImageProxy imageProxy, CameraScanCallback callback) {
        barcodeScanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    if (!barcodes.isEmpty()) {
                        String rawValue = barcodes.get(0).getRawValue();
                        if (rawValue != null && rawValue.length() >= 8) {
                            Log.i(TAG, "Barcode detected: " + rawValue);
                            processResult(rawValue, imageProxy, callback);
                            return;
                        }
                    }
                    processTextRecognition(image, imageProxy, callback);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Barcode detection failed: " + e.getMessage());
                    processTextRecognition(image, imageProxy, callback);
                });
    }

    private void processTextRecognition(InputImage image, ImageProxy imageProxy, CameraScanCallback callback) {
        textRecognizer.process(image)
                .addOnSuccessListener(text -> {
                    String candidate = extractCandidateFromText(text.getText());
                    Log.i(TAG, "Text extracted: " + candidate);
                    processResult(candidate, imageProxy, callback);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Text recognition failed: " + e.getMessage());
                    imageProxy.close();
                    callback.onScanError(e);
                });
    }

    private String extractCandidateFromText(String fullText) {
        Matcher matcherBarcode = Pattern.compile("\\b\\d{7,}\\b").matcher(fullText);
        if (matcherBarcode.find()) return matcherBarcode.group();

        Matcher matcherPLU = Pattern.compile("\\b\\d{4,5}\\b").matcher(fullText);
        if (matcherPLU.find()) return matcherPLU.group();

        return "";
    }

    private synchronized void processResult(String result, ImageProxy imageProxy, CameraScanCallback callback) {
        imageProxy.close();

        if (result.isEmpty()) return;

        lastResults.add(result);
        if (lastResults.size() > 2) lastResults.removeFirst();

        if (lastResults.size() == 2 && lastResults.getFirst().equals(lastResults.getLast())) {
            Log.i(TAG, "Confirmed scan: " + result);
            lastConfirmedScanTime = System.currentTimeMillis();
            playTone();
            callback.onScanResult(result);
            lastResults.clear();
        }
    }

    private void playTone() {
        Log.i(TAG, "Playing tone...");
        int toneType = ToneGenerator.TONE_PROP_BEEP;
        toneGenerator.startTone(toneType, 150); //TODO MOVE THIS BACK TO 150 also provide some way to adjust this in app
    }
}