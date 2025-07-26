package com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeDetail.recipeDetailComponets

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun RecipeDialogs(
    showDeleteDialog: Boolean,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    showDuplicateNameDialog: Boolean,
    onDismissDuplicate: () -> Unit,
    showNameRequiredDialog: Boolean,
    onDismissMissingName: () -> Unit
) {
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text("Delete Recipe") },
            text = { Text("Are you sure you want to delete this recipe? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = onDismissDelete) { Text("Cancel") }
            }
        )
    }

    if (showDuplicateNameDialog) {
        AlertDialog(
            onDismissRequest = onDismissDuplicate,
            title = { Text("Duplicate Recipe Name") },
            text = { Text("Another recipe already uses this name. Please choose a different one.") },
            confirmButton = {
                TextButton(onClick = onDismissDuplicate) { Text("OK") }
            }
        )
    }

    if (showNameRequiredDialog) {
        AlertDialog(
            onDismissRequest = onDismissMissingName,
            title = { Text("Missing Name") },
            text = { Text("Please enter a name for the recipe.") },
            confirmButton = {
                TextButton(onClick = onDismissMissingName) { Text("OK") }
            }
        )
    }
}