package com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeDetail

import RecipeEditUiState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.recipe.ui.componets.ingredientChips.IngredientChip
import com.example.possiblythelastnewproject.features.recipe.ui.componets.ingredientChips.IngredientChipEditor
import com.example.possiblythelastnewproject.features.recipe.ui.componets.ingredientChips.LazyFlowRow
import com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeCreation.RecipeIngredientUI
import com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeDetail.Solid.EditableField
import com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeDetail.Solid.ReadOnlyField
import com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeDetail.Solid.RecipeColorPicker

@Composable
fun RecipeDetailForm(
    uiState: RecipeEditUiState,
    isEditing: Boolean,
    pantryItems: List<PantryItem>,
    onFieldChange: (RecipeEditUiState.() -> RecipeEditUiState) -> Unit,
    onIngredientsChange: (List<RecipeIngredientUI>) -> Unit,
    onColorSelect: (Color) -> Unit,
    onRequestCreatePantryItem: suspend (String) -> PantryItem,
    onToggleShoppingStatus: (PantryItem) -> Unit,
    onSave: () -> Unit,
    onCancel: (hasChanges: Boolean, requestExit: () -> Unit, exitCleanly: () -> Unit) -> Unit,
    hasChanges: Boolean
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize(tween(300)),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!isEditing) {
                ReadOnlyField("Name", uiState.name.text)
                ReadOnlyField("Temperature", uiState.temp.text)
                ReadOnlyField("Prep Time", uiState.prepTime.text)
                ReadOnlyField("Cook Time", uiState.cookTime.text)
                ReadOnlyField("Category", uiState.category.text)

                Text("Ingredients", style = MaterialTheme.typography.labelMedium)

                val pantryMap = pantryItems.associateBy { it.id }
                if (uiState.ingredients.isEmpty()) {
                    Text(
                        "No ingredients yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyFlowRow(
                        items = uiState.ingredients,
                        horizontalSpacing = 8.dp,
                        verticalSpacing = 8.dp,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(top = 4.dp)
                    ) { ing ->
                        val pantryItem = pantryMap[ing.pantryItemId]
                        IngredientChip(
                            ingredient = ing,
                            pantryItem = pantryItem,
                            isEditable = false
                        )
                    }
                }

                HorizontalDivider()
                ReadOnlyField("Instructions", uiState.instructions.text)

                Text("Card Color", style = MaterialTheme.typography.labelMedium)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .border(1.dp, Color.Black, CircleShape)
                        .background(uiState.cardColor, CircleShape)
                )
            } else {
                EditableField("Name", uiState.name) {
                    onFieldChange { copy(name = it) }
                }
                EditableField("Temperature", uiState.temp) {
                    onFieldChange { copy(temp = it) }
                }
                EditableField("Prep Time", uiState.prepTime) {
                    onFieldChange { copy(prepTime = it) }
                }
                EditableField("Cook Time", uiState.cookTime) {
                    onFieldChange { copy(cookTime = it) }
                }
                EditableField("Category", uiState.category) {
                    onFieldChange { copy(category = it) }
                }

                IngredientChipEditor(
                    ingredients = uiState.ingredients,
                    allPantryItems = pantryItems,
                    onIngredientsChange = onIngredientsChange,
                    onRequestCreatePantryItem = onRequestCreatePantryItem,
                    onToggleShoppingStatus = onToggleShoppingStatus
                )

                HorizontalDivider()

                EditableField(
                    label = "Instructions",
                    state = uiState.instructions,
                    singleLine = false,
                    heightDp = 140
                ) {
                    onFieldChange { copy(instructions = it) }
                }

                RecipeColorPicker(
                    selectedColor = uiState.cardColor,
                    onColorSelected = { newColor ->
                        onFieldChange { copy(cardColor = newColor) }
                        onColorSelect(newColor)
                    }
                )

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onSave,
                        enabled = hasChanges,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }
                    OutlinedButton(
                        onClick = {
                            if (hasChanges) {
                                onCancel(true, { /* handled by parent */ }, {})
                            } else {
                                onCancel(false, {}, {})
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}