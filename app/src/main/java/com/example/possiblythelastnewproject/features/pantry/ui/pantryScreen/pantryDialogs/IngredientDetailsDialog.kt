package com.example.possiblythelastnewproject.features.pantry.ui.pantryScreen.pantryDialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.pantry.ui.pantryScreen.IngredientCard

@Composable
fun IngredientDetailsDialog(
    item: PantryItem,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onScanClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(item.name) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                IngredientCard(
                    ingredient = item.name,
                    quantity = item.quantity,
                    modifier = Modifier.fillMaxWidth(),
                    category = item.category,
                    imageUri = item.imageUri
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onScanClick) {
                        Text(
                            if (item.scanCode.isNullOrBlank()) "Link PLU or Barcode"
                            else "Update PLU/Barcode"
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text("Category:", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = item.category.ifBlank { "None" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(8.dp))

                Text("Scan code:", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = item.scanCode ?: "None",
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) { Text("Close") }
                TextButton(onClick = onEdit) { Text("Edit") }
            }
        }
    )
}