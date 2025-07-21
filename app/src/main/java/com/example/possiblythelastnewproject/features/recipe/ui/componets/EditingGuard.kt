package com.example.possiblythelastnewproject.features.recipe.ui.componets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

val LocalEditingGuard = compositionLocalOf<EditingGuard> {
    error("No EditingGuard provided")
}

class EditingGuard {
    var isEditing by mutableStateOf(false)
    var showDiscardDialog by mutableStateOf(false)

    private var onDiscardConfirmed: (() -> Unit)? = null
    private var onRollbackConfirmed: (() -> Unit)? = null

    fun markEditing(active: Boolean = true) {
        isEditing = active
    }

    fun resetEditing() {
        isEditing = false
    }

    fun requestExit(
        rollback: () -> Unit,
        thenExit: () -> Unit
    ) {
        onRollbackConfirmed = rollback
        onDiscardConfirmed = thenExit
        showDiscardDialog = true
    }

    fun confirmExit() {
        showDiscardDialog = false
        onRollbackConfirmed?.invoke()
        onDiscardConfirmed?.invoke()
        clear()
    }

    fun cancelExit() {
        showDiscardDialog = false
        clear()
    }

    fun guardedExit(
        hasChanges: Boolean,
        rollback: () -> Unit,
        thenExit: () -> Unit,
        cleanExit: () -> Unit
    ) {
        if (isEditing && hasChanges) {
            requestExit(rollback, thenExit)
        } else {
            cleanExit()
        }
    }

    private fun clear() {
        onRollbackConfirmed = null
        onDiscardConfirmed = null
    }
}

@Composable
fun EditingGuardDialog(guard: EditingGuard) {
    if (!guard.showDiscardDialog) return

    AlertDialog(
        onDismissRequest = { guard.cancelExit() },
        title = { Text("Discard changes?", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("You have unsaved changes. What would you like to do?")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text(
                        "Changes will be lost if you discard.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { guard.confirmExit() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Discard Changes")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { guard.cancelExit() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Keep Editing")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}