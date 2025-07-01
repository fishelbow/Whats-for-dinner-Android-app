package com.example.possiblythelastnewproject.core.ui

import androidx.compose.runtime.*

val LocalEditingGuard = compositionLocalOf<EditingGuard> {
    error("No EditingGuard provided")
}

class EditingGuard {
    var isEditing by mutableStateOf(false)
    var showDiscardDialog by mutableStateOf(false)
    private var onDiscardConfirmed: (() -> Unit)? = null

    fun requestExit(onConfirm: () -> Unit) {
        onDiscardConfirmed = onConfirm
        showDiscardDialog = true
    }

    fun confirmExit() {
        showDiscardDialog = false
        isEditing = false
        onDiscardConfirmed?.invoke()
        onDiscardConfirmed = null
    }

    fun cancelExit() {
        showDiscardDialog = false
        onDiscardConfirmed = null
    }
}