package com.example.possiblythelastnewproject.features.pantry.ui.pantryScreen.pantryDialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.possiblythelastnewproject.features.scan.ui.ScanningTab

@Composable
fun BarcodeScanDialog(
    onDismiss: () -> Unit,
    onScanned: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            ScanningTab(
                shouldScan = true,
                onScanResult = {
                    onScanned(it)
                    onDismiss()
                }
            )
        }
    }
}