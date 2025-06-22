package com.example.possiblythelastnewproject.features.pantry.ui.componets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun IngredientSearchBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onAddNewIngredient: () -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        TextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            placeholder = { Text("Search Pantry") },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon"
                )
            },
            trailingIcon = {
                IconButton(onClick = {
                    focusManager.clearFocus()
                    onAddNewIngredient()
                }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Ingredient"
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
fun IngredientSearchBarPreview() {
    // Replace with an actual FocusManager when previewing if needed
    val dummyFocusManager = LocalFocusManager.current
    MaterialTheme {
        IngredientSearchBar(
            searchQuery = "",
            onQueryChange = {},
            onAddNewIngredient = {},
            focusManager = dummyFocusManager
        )
    }
}