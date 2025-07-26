package com.example.possiblythelastnewproject.core.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.example.possiblythelastnewproject.core.ui.navigation.MainScreen
import com.example.possiblythelastnewproject.core.ui.theme.PossiblyTheLastNewProjectTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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