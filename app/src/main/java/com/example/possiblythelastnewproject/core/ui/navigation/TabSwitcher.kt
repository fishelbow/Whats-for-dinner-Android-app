package com.example.possiblythelastnewproject.core.ui.navigation

import com.example.possiblythelastnewproject.features.recipe.ui.componets.EditingGuard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun handleTabSwitch(
    index: Int,
    currentPageSetter: (Int) -> Unit,
    editingGuard: EditingGuard,
    rollback: () -> Unit = {},
    coroutineScope: CoroutineScope
) {
    if (!editingGuard.isEditing) {
        currentPageSetter(index)
    } else {
        coroutineScope.launch {
            editingGuard.requestExit(
                rollback = { coroutineScope.launch { rollback() } },
                thenExit = { currentPageSetter(index) }
            )
        }
    }
}