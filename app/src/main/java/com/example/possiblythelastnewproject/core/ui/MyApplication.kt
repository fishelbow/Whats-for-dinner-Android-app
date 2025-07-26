package com.example.possiblythelastnewproject.core.ui

import android.app.Application
import com.example.possiblythelastnewproject.core.utils.StartupSweeper
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application() {
    @Inject
    lateinit var startupSweeper: StartupSweeper

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            startupSweeper.run(this@MyApplication)
        }
    }
}