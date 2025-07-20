package com.example.possiblythelastnewproject.features.recipe.ui.componets

import androidx.compose.runtime.*

val LocalEditingGuard = compositionLocalOf<EditingGuard> {
    error("No EditingGuard provided")
}

class EditingGuard {
    var isEditing by mutableStateOf(false)
    var showDiscardDialog by mutableStateOf(false)

    private var onDiscardConfirmed: (() -> Unit)? = null
    private var onRollbackConfirmed: (() -> Unit)? = null

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
        onRollbackConfirmed = null
        onDiscardConfirmed = null
    }

    fun cancelExit() {
        showDiscardDialog = false
        onRollbackConfirmed = null
        onDiscardConfirmed = null
    }
}