package com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeCreation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.recipe.ui.componets.ingredientChips.IngredientChipEditor

@Composable
fun RecipeFormCard(
    name: String,
    onNameChange: (String) -> Unit,
    temp: String,
    onTempChange: (String) -> Unit,
    prepTime: String,
    onPrepTimeChange: (String) -> Unit,
    cookTime: String,
    onCookTimeChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    cardColor: Color,
    onCardColorChange: (Color) -> Unit,
    colorOptions: List<Color>,
    ingredients: List<RecipeIngredientUI>,
    onIngredientsChange: (List<RecipeIngredientUI>) -> Unit,
    instructions: String,
    onInstructionsChange: (String) -> Unit,
    onSave: () -> Unit, // ✅ fixed here
    onCancel: () -> Unit,
    pantryItems: List<PantryItem>,
    onRequestCreatePantryItem: suspend (String) -> PantryItem,
    onToggleShoppingStatus: (PantryItem) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FormField("Recipe Name", name, onNameChange)
            FormField("Temperature", temp, onTempChange)
            FormField("Prep Time", prepTime, onPrepTimeChange)
            FormField("Cook Time", cookTime, onCookTimeChange)
            FormField("Category", category, onCategoryChange)

            Text("Card Color", style = MaterialTheme.typography.labelMedium)
            ColorPicker(
                selectedColor = cardColor,
                colorOptions = colorOptions,
                onColorSelected = onCardColorChange
            )

            Text("Ingredients", style = MaterialTheme.typography.labelMedium)
            IngredientChipEditor(
                ingredients = ingredients,
                onIngredientsChange = onIngredientsChange,
                allPantryItems = pantryItems,
                onRequestCreatePantryItem = onRequestCreatePantryItem,
                onToggleShoppingStatus = onToggleShoppingStatus
            )

            FormField(
                label = "Instructions",
                value = instructions,
                onValueChange = onInstructionsChange,
                singleLine = false,
                heightDp = 140
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }

                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}