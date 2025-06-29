package com.example.possiblythelastnewproject.core.data.sandbox

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

@Composable
fun rememberDbBackupLaunchers(viewModel: DbBackupViewModel): Pair<() -> Unit, () -> Unit> {
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.importDb(it) } }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri -> uri?.let { viewModel.exportDb(it) } }

    return Pair(
        { importLauncher.launch(arrayOf("*/*")) },
        { exportLauncher.launch("room_backup.db") }
    )
}