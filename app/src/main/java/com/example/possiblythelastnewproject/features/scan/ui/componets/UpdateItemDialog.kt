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
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.pantry.ui.componets.IngredientCard

@Composable
fun UpdateItemDialog(
    item: PantryItem,
    onDismiss: () -> Unit,
    onConfirmUpdate: (PantryItem) -> Unit
) {

    val validItem = item.copy(imageUri = item.imageUri) // or however you're validating

    AlertDialog(
        onDismissRequest = {},
        title = { Text("Item Already Exists") },
        text = {
            Column {
                Text("Scanned item matches:")
                Spacer(Modifier.height(8.dp))
                IngredientCard(
                    ingredient = item.name,
                    quantity = item.quantity,
                    imageUri = validItem.imageUri
                )
                Spacer(Modifier.height(8.dp))
                Text("Would you like to update or keep it as is?")
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirmUpdate(item.copy(quantity = item.quantity + 1)) }) {
                Text("Add 1 to Quantity")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}