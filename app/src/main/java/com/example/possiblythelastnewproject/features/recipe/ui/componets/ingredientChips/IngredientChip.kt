package com.example.possiblythelastnewproject.features.recipe.ui.componets.ingredientChips

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// Your models
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeCreation.RecipeIngredientUI

@Composable
fun IngredientChip(
    ingredient: RecipeIngredientUI,
    pantryItem: PantryItem?,
    isEditable: Boolean,
    onToggleShoppingStatus: ((PantryItem) -> Unit)? = null,
    onRemove: (() -> Unit)? = null
) {
    val inCart = pantryItem?.addToShoppingList ?: ingredient.includeInShoppingList

    val labelContent: @Composable () -> Unit = {
        Column(
            modifier = Modifier.padding(end = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "${ingredient.amountRequired} × ${pantryItem?.name ?: ingredient.name}",
                maxLines = 2,
                overflow = TextOverflow.Clip,
                style = MaterialTheme.typography.bodySmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (inCart) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = "In Shopping List",
                        modifier = Modifier.size(16.dp)
                    )
                }
                if (ingredient.hasScanCode) {
                    Icon(
                        Icons.Default.QrCode2,
                        contentDescription = "Has Scan Code",
                        modifier = Modifier.size(16.dp)
                    )
                }
                if (ingredient.pantryItemId == null) {
                    Icon(
                        Icons.Default.NewReleases,
                        contentDescription = "Custom Ingredient",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

    if (isEditable) {
        InputChip(
            selected = inCart,
            onClick = {
                pantryItem?.let {
                    onToggleShoppingStatus?.invoke(it.copy(addToShoppingList = !inCart))
                }
            },
            label = labelContent,
            trailingIcon = {
                onRemove?.let {
                    IconButton(onClick = it) {
                        Icon(Icons.Default.Close, contentDescription = "Remove")
                    }
                }
            },
            modifier = Modifier.widthIn(min = 120.dp, max = 300.dp)
        )
    } else {
        AssistChip(
            onClick = {},
            label = labelContent,
            modifier = Modifier.widthIn(min = 120.dp, max = 300.dp)
        )
    }
}