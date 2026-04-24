package com.soloretreat.ui.integration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soloretreat.ui.components.RetreatAppBar

@Composable
fun JournalEntryScreen(
    entryId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: JournalViewModel = hiltViewModel()
) {
    val editing by viewModel.editing.collectAsState()
    var text by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var hydrated by remember { mutableStateOf(entryId == null) }

    LaunchedEffect(entryId) {
        if (entryId != null) {
            viewModel.loadEntry(entryId)
        }
    }

    LaunchedEffect(editing, entryId) {
        if (entryId != null && !hydrated) {
            editing?.let {
                text = it.entryText
                tags = it.tags
                hydrated = true
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearEditing() }
    }

    Scaffold(
        topBar = {
            RetreatAppBar(
                title = if (entryId == null) "New Journal Entry" else "Edit Entry",
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Write your reflections...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                minLines = 10
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("Tags (comma-separated)") },
                placeholder = { Text("insight, challenge, gratitude...") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (entryId == null) {
                        viewModel.saveEntry(text, tags)
                    } else {
                        viewModel.updateEntry(entryId, text, tags)
                    }
                    onNavigateBack()
                },
                enabled = text.isNotBlank() && hydrated,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.height(16.dp))
                Text(if (entryId == null) "Save Entry" else "Update Entry")
            }
        }
    }
}
