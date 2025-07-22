package com.example.possiblythelastnewproject.core.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.possiblythelastnewproject.core.data.AppDatabase
import com.example.possiblythelastnewproject.core.ui.navigation.MainScreen
import com.example.possiblythelastnewproject.core.ui.theme.PossiblyTheLastNewProjectTheme
import com.example.possiblythelastnewproject.core.utils.OrphanHunter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
/*
        // 🧹 Trigger OrphanHunter startup scan once dependencies are ready
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(applicationContext)
            val recipeDao = db.recipeDao()
            val pantryDao = db.pantryItemDao()

            val orphans = OrphanHunter.runAudit(applicationContext, recipeDao, pantryDao)
            Log.d("OrphanHunter", "🚀 Startup sweep complete. Found ${orphans.size} orphans.")
        }
*/
        setContent {
            PossiblyTheLastNewProjectTheme {
                // automatically switch themes based on system settings.
                // In Compose, you can obtain the view model instance using the viewModel() extension
                // which will be provided by Hilt.
                MainScreen()
            }
        }
    }
}