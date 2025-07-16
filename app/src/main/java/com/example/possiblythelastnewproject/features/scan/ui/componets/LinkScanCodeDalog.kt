package com.example.possiblythelastnewproject.features.scan.ui.componets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.possiblythelastnewproject.features.pantry.ui.componets.IngredientCard
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem as PantryItem1

@Composable
fun LinkScanCodeDialog(
    item: PantryItem1,
    onDismiss: () -> Unit,
    onConfirmLink: (PantryItem1) -> Unit
) {
    val validItem = item.copy(/* any transformations or validations here */)

    AlertDialog(
        onDismissRequest = {},
        title = { Text("Name Conflict") },
        text = {
            Column {
                Text("A pantry item named \"${item.name}\" already exists:")
                Spacer(Modifier.height(8.dp))
                IngredientCard(
                    ingredient = item.name,
                    quantity = item.quantity,
                    imageUri = validItem.imageUri
                )
                Spacer(Modifier.height(8.dp))
                Text("Would you like to link the scanned code to this item?")
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirmLink(item) }) {
                Text("Link Code")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}