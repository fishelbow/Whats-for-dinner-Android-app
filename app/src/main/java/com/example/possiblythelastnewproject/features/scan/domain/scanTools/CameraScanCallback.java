package com.example.possiblythelastnewproject.features.scan.domain.scanTools;

public interface CameraScanCallback {
    void onScanResult(String scannedData);
    void onScanError(Exception e);
}