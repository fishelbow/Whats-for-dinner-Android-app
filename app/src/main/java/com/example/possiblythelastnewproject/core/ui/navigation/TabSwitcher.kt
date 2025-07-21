package com.example.possiblythelastnewproject.core.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.possiblythelastnewproject.features.recipe.ui.componets.EditingGuard

sealed class TabItem(val title: String, val icon: ImageVector) {
    data object Recipes : TabItem("Recipes", Icons.AutoMirrored.Filled.List)
    data object Pantry : TabItem("Pantry", Icons.Filled.Kitchen)
    data object Shopping : TabItem("Shopping", Icons.Filled.Checklist)
    data object Scanning : TabItem("Scanning", Icons.Filled.CameraAlt)
}

@Composable
fun TabSwitcher(
    tabs: List<TabItem>,
    currentPage: Int,
    onTabClicked: (Int, TabItem) -> Unit
) {
    TabRow(selectedTabIndex = currentPage) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = currentPage == index,
                onClick = { onTabClicked(index, tab) },
                icon = { Icon(tab.icon, contentDescription = tab.title) },
                text = { Text(tab.title) }
            )
        }
    }
}

fun handleTabSwitch(
    index: Int,
    currentPageSetter: (Int) -> Unit,
    editingGuard: EditingGuard,
    rollback: () -> Unit
) {
    editingGuard.guardedExit(
        hasChanges = true,
        rollback = rollback,
        thenExit = {
            editingGuard.isEditing = false
            currentPageSetter(index)
        },
        cleanExit = {
            editingGuard.isEditing = false
            currentPageSetter(index)
        }
    )
}