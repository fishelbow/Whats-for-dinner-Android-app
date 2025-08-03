package com.example.possiblythelastnewproject.features.pantry.ui.pantryScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem

@Composable
fun PantryGridSection(filteredItems: List<PantryItem>, onItemClick: (PantryItem) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No ingredients found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredItems) { item ->
                    IngredientCard(
                        ingredient = item.name.truncateWithEllipsis(20),
                        quantity = item.quantity,
                        imageUri = item.imageUri,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemClick(item) }
                    )
                }
            }
        }
    }
}

fun String.truncateWithEllipsis(maxLength: Int = 20): String {
    return if (this.length > maxLength) this.take(maxLength) + "…" else this
}