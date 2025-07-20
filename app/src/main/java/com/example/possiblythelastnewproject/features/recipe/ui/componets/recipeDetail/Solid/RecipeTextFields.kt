package com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeDetail.Solid

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp



@Composable
fun ReadOnlyField(label: String, value: String) {
    Text(label, style = MaterialTheme.typography.labelMedium)
    Text(value, style = MaterialTheme.typography.bodyLarge)
    HorizontalDivider()
}


@Composable
fun EditableField(
    label: String,
    state: TextFieldValue,
    singleLine: Boolean = true,
    heightDp: Int? = null,
    onValueChange: (TextFieldValue) -> Unit
) {
    Text(label, style = MaterialTheme.typography.labelMedium)
    OutlinedTextField(
        value = state,
        onValueChange = onValueChange,
        singleLine = singleLine,
        placeholder = { Text("Enter $label") },
        modifier = Modifier
            .fillMaxWidth()
            .then(if (heightDp != null) Modifier.height(heightDp.dp) else Modifier)
    )
    HorizontalDivider()
}