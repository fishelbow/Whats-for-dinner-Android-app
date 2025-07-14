package com.example.possiblythelastnewproject.features.shoppingList.ui.componets

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.ShoppingListItem

@Composable
fun ShoppingListCard(
    item: ShoppingListItem,
    onCheckToggled: (ShoppingListItem) -> Unit,
    onLongPress: (ShoppingListItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        ShoppingListRow(
            item = item,
            onCheckToggled = onCheckToggled,
            onLongPress = onLongPress,
            modifier = Modifier.padding(12.dp)
        )
    }
}