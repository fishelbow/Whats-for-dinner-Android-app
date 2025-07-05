package com.example.possiblythelastnewproject.features.shoppingList.ui.componets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.ShoppingListItem
import com.example.possiblythelastnewproject.features.shoppingList.ui.model.CategorizedShoppingItem

@Composable
fun ShoppingListScreen(
    listName: String,
    categorizedItems: List<CategorizedShoppingItem>,
    onCheckToggled: (ShoppingListItem) -> Unit
) {
    var hideChecked by remember { mutableStateOf(false) }

    val groupedItems = remember(hideChecked, categorizedItems) {
        categorizedItems
            .filter { !hideChecked || !it.item.isChecked }
            .groupBy { it.category.ifBlank { "Uncategorized" } }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = listName,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(8.dp))
                HideCheckedToggle(hideChecked = hideChecked) {
                    hideChecked = !hideChecked
                }
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            groupedItems.forEach { (category, items) ->
                item {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(items) { categorized ->
                    ShoppingListRow(
                        item = categorized.item,
                        onCheckToggled = onCheckToggled
                    )
                }
            }
        }
    }
}

@Composable
fun HideCheckedToggle(
    hideChecked: Boolean,
    onToggle: () -> Unit
) {
    TextButton(onClick = onToggle) {
        Text(if (hideChecked) "Show Checked" else "Hide Checked")
    }
}