package com.example.possiblythelastnewproject.core.data.sandbox

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DbBackupViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    var result by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)
        private set

    fun exportDb(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            val success = DbFileManager.exportDatabase(context, uri)
            result = if (success) "✅ Database exported successfully" else "❌ Export failed"
            isLoading = false
        }
    }

    fun importDb(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            val success = DbFileManager.importDatabase(context, uri)
            result = if (success) "✅ Database imported successfully" else "❌ Import failed"
            isLoading = false
        }
    }

    fun clearResult() {
        result = null
    }
}