import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.unit.dp

@Composable
fun IngredientSearchBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onAddNewIngredient: () -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier
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
                Row {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Search"
                            )
                        }
                    }
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        onAddNewIngredient()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Ingredient"
                        )
                    }
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