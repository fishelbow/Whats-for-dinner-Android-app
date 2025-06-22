package com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeCreation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true,
    heightDp: Int? = null
) {
    Text(label, style = MaterialTheme.typography.labelMedium)
    Spacer(modifier = Modifier.height(4.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("Enter $label") },
        singleLine = singleLine,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (heightDp != null) Modifier.height(heightDp.dp) else Modifier)
    )
}
@Composable
fun ColorPicker(
    selectedColor: Color,
    colorOptions: List<Color>,
    onColorSelected: (Color) -> Unit
) {
    val rows = colorOptions.chunked(6)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                row.forEach { color ->
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (color == selectedColor) 3.dp else 1.dp,
                                color = if (color == selectedColor) MaterialTheme.colorScheme.primary else Color.Gray,
                                shape = CircleShape
                            )
                            .clickable { onColorSelected(color) }
                    )
                }
            }
        }
    }
}