package com.example.possiblythelastnewproject.core.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import com.example.possiblythelastnewproject.features.recipe.ui.componets.EditingGuard

// 🧭 Tab definitions
sealed class TabItem(val title: String, val icon: ImageVector) {
    data object Recipes : TabItem("Recipes", Icons.AutoMirrored.Filled.List)
    data object Pantry : TabItem("Pantry", Icons.Filled.Kitchen)
    data object Shopping : TabItem("Shopping", Icons.Filled.Checklist)
    data object Scanning : TabItem("Scanning", Icons.Filled.CameraAlt)
}

// 🎯 Composable tab row
@Composable
fun TabSwitcher(
    tabs: List<TabItem>,
    currentPage: Int,
    onTabClicked: (Int, TabItem) -> Unit,
    tabsEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val visualModifier = modifier.alpha(if (tabsEnabled) 1f else 0.4f)

    TabRow(
        selectedTabIndex = currentPage,
        modifier = visualModifier
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = currentPage == index,
                onClick = { if (tabsEnabled) onTabClicked(index, tab) },
                enabled = tabsEnabled,
                icon = { Icon(tab.icon, contentDescription = tab.title) },
                text = { Text(tab.title) }
            )
        }
    }
}

// 🧠 Unified tab switch logic
fun createTabClickHandler(
    currentTab: TabItem,
    editingGuard: EditingGuard,
    rollback: () -> Unit,
    navMap: Map<TabItem, NavHostController>,
    setCurrentPage: (Int) -> Unit
): (Int, TabItem) -> Unit = { targetIndex, _ ->

    val navController = navMap[currentTab]

    editingGuard.guardedExit(
        hasChanges = true,
        rollback = {
            rollback()
            navController?.popBackStack("recipes_main", inclusive = false)
        },
        thenExit = {
            editingGuard.isEditing = false
            setCurrentPage(targetIndex)
        },
        cleanExit = {
            editingGuard.isEditing = false
            navController?.popBackStack("recipes_main", inclusive = false)
            setCurrentPage(targetIndex)
        }
    )
}