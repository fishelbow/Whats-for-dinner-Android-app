package com.example.possiblythelastnewproject.features.shoppingList.ui.componets

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.ShoppingListItem

@Composable
fun ShoppingListRow(
    item: ShoppingListItem,
    onCheckToggled: (ShoppingListItem) -> Unit,
    onLongPress: (ShoppingListItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onCheckToggled(item) },
                onLongClick = { onLongPress(item) }
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = { onCheckToggled(item) }
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            val textStyle = MaterialTheme.typography.bodyLarge.let { base ->
                if (item.isChecked) base.copy(textDecoration = TextDecoration.LineThrough) else base
            }

            Text(
                text = item.name,
                style = textStyle
            )

            item.quantity
                .takeIf { it.isNotBlank() }
                ?.let { raw ->
                    val formatted = raw.toDoubleOrNull()?.toInt()?.toString() ?: raw
                    Text(
                        text = "need: $formatted",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewShoppingListRow() {
    MaterialTheme {
        ShoppingListRow(
            item = ShoppingListItem(
                id = 1L,
                listId = 1L,
                pantryItemId = 101L,
                name = "Bananas",
                quantity = "6",
                isChecked = false,
                isGenerated = false
            ),
            onCheckToggled = {},
            onLongPress = {},
            modifier = Modifier.padding(12.dp)
        )
    }
}