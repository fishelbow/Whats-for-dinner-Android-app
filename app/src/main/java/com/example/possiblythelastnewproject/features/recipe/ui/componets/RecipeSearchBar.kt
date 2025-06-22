package com.example.possiblythelastnewproject.features.recipe.ui.componets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun RecipeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onAddNewRecipe: () -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search Recipes") },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon"
                )
            },
            trailingIcon = {
                IconButton(onClick = {
                    // Clear focus and trigger new recipe creation.
                    focusManager.clearFocus()
                    onAddNewRecipe()
                }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Recipe"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeSearchBarPreview() {
    // For preview purposes, we use the local focus manager.
    androidx.compose.ui.platform.LocalFocusManager.current?.let { fm ->
        MaterialTheme {
            RecipeSearchBar(
                query = "",
                onQueryChange = {},
                onAddNewRecipe = {},
                focusManager = fm
            )
        }
    }
}