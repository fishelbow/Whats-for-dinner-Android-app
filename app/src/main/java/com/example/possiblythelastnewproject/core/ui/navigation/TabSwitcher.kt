package com.example.possiblythelastnewproject.core.ui.navigation

import com.example.possiblythelastnewproject.features.recipe.ui.componets.EditingGuard
import kotlinx.coroutines.CoroutineScope

fun handleTabSwitch(
    index: Int,
    currentPageSetter: (Int) -> Unit,
    editingGuard: EditingGuard,
    rollback: () -> Unit,
    coroutineScope: CoroutineScope
) {
    if (editingGuard.isEditing) {
        editingGuard.requestExit(
            rollback = rollback,
            thenExit = {
                editingGuard.isEditing = false
                currentPageSetter(index)
            }
        )
    } else {
        editingGuard.isEditing = false
        currentPageSetter(index)
    }
}