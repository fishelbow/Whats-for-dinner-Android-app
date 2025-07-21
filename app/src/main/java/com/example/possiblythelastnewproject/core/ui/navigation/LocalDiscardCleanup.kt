package com.example.possiblythelastnewproject.core.ui.navigation

import androidx.compose.runtime.compositionLocalOf

val LocalDiscardCleanup = compositionLocalOf<(() -> Unit)?> { null }