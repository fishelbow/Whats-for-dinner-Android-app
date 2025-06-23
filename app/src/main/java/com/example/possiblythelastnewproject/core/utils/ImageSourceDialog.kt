package com.example.possiblythelastnewproject.core.utils

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ImageSourceDialog(
    onDismiss: () -> Unit,
    onPickGallery: () -> Unit,
    onTakePhoto: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Image Source") },
        text = {
            // You can modify this Column to style the buttons as desired.
            androidx.compose.foundation.layout.Column {
                TextButton(onClick = {
                    onTakePhoto()
                    onDismiss()
                }) {
                    Text("Take Photo")
                }
                TextButton(onClick = {
                    onPickGallery()
                    onDismiss()
                }) {
                    Text("Choose from Gallery")
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}