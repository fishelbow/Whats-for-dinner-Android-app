package com.example.possiblythelastnewproject.debug

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun DebugToolsScreen() {
    val viewModel: DebugViewModel = hiltViewModel()

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = { viewModel.loadTestData() }) {
            Text("Load Test Data")
        }
    }
}