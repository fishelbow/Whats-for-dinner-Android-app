package com.example.possiblythelastnewproject.core.ui

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.possiblythelastnewproject.core.ui.navigation.PantryNavHost
import com.example.possiblythelastnewproject.core.ui.navigation.RecipesNavHost
import com.example.possiblythelastnewproject.core.ui.navigation.ScanningNavHost
import com.example.possiblythelastnewproject.core.ui.navigation.ShoppingNavHost
import com.example.possiblythelastnewproject.core.ui.navigation.TabItem
import com.example.possiblythelastnewproject.core.ui.navigation.TabSwitcher
import com.example.possiblythelastnewproject.core.ui.navigation.createTabClickHandler
import com.example.possiblythelastnewproject.core.utils.MediaOrphanHunter
import com.example.possiblythelastnewproject.features.pantry.data.PantryRepository
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipeRepository
import com.example.possiblythelastnewproject.features.recipe.ui.componets.EditingGuard
import com.example.possiblythelastnewproject.features.recipe.ui.componets.EditingGuardDialog
import com.example.possiblythelastnewproject.features.recipe.ui.componets.LocalEditingGuard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext


// ────────────────────────────────────────────────
// 1) MainScreen – without horizontal swiping
// ────────────────────────────────────────────────

@Composable
fun MainScreen(
) {
    val editingGuard = remember { EditingGuard() }
    val discardDraftCleanup = remember { mutableStateOf<(() -> Unit)?>(null) }


    val rollback: () -> Unit = {
        println("🔄 Rollback triggered from tab switch")
        discardDraftCleanup.value?.invoke()
    }

    val context = LocalContext.current
    val tabs = listOf(TabItem.Recipes, TabItem.Pantry, TabItem.Shopping, TabItem.Scanning)
    var currentPage by remember { mutableIntStateOf(0) }
    val navMap = tabs.associateWith { rememberNavController() }

    val tabClickHandler = createTabClickHandler(
        currentTab = tabs[currentPage],
        editingGuard = editingGuard,
        rollback = rollback,
        navMap = navMap,
        setCurrentPage = { currentPage = it }
    )

    CompositionLocalProvider(LocalEditingGuard provides editingGuard) {
        Scaffold { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {

                TabSwitcher(
                    tabs = tabs,
                    currentPage = currentPage,
                    onTabClicked = tabClickHandler,
                    tabsEnabled = !editingGuard.isEditing
                )
                when (tabs[currentPage]) {
                    TabItem.Recipes -> RecipesNavHost(
                        navController = navMap[TabItem.Recipes]!!,
                        registerDiscardCleanup = { discardDraftCleanup.value = it }
                    )
                    TabItem.Pantry -> PantryNavHost(navMap[TabItem.Pantry]!!)
                    TabItem.Shopping -> ShoppingNavHost(navMap[TabItem.Shopping]!!)
                    TabItem.Scanning -> ScanningNavHost(navMap[TabItem.Scanning]!!)
                }

                EditingGuardDialog(guard = editingGuard)
            }
        }
    }
}