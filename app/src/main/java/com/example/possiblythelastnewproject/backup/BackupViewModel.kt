package com.example.possiblythelastnewproject.backup

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupRepo: ZipBackupRepository
) : ViewModel() {

    var progress: Float by mutableStateOf(0f)
    var statusMessage: String? by mutableStateOf(null)

    var isLoading by mutableStateOf(false)
        private set

    var result: String? by mutableStateOf(null)
        private set

    fun clearResult() {
        result = null
        progress = 0f
        statusMessage = null
    }

    fun handleImportZip(uri: Uri) {
        launchWithLoading {
            result = backupRepo.importZipToDatabase(uri) { pct, msg ->
                progress = pct
                statusMessage = msg
            }
        }
    }

    fun handleExportZip(uri: Uri) {
        launchWithLoading {
            result = backupRepo.exportDatabaseToZip(uri) { pct, msg ->
                progress = pct
                statusMessage = msg
            }
        }
    }

    private fun launchWithLoading(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            progress = 0f
            statusMessage = "⏳ Preparing…"
            try {
                block()
            } catch (e: Exception) {
                result = "❌ Error: ${e.localizedMessage}"
                statusMessage = result
                progress = 1f
            } finally {
                isLoading = false
            }
        }
    }
}