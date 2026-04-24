package com.soloretreat.ui.preparation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soloretreat.data.model.Phase
import com.soloretreat.ui.components.RetreatAppBar
import com.soloretreat.util.TimeUtils

@Composable
fun PreparationDashboardScreen(
    onScheduleBuilder: () -> Unit,
    onChantCatalog: () -> Unit,
    onTalkCatalog: () -> Unit,
    onChecklist: () -> Unit,
    onEightPrecepts: () -> Unit,
    onStartRetreat: () -> Unit,
    viewModel: PreparationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showResetConfirm by remember { mutableStateOf(false) }
    var showEditDates by remember { mutableStateOf(false) }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset Retreat Data?") },
            text = { Text("This permanently deletes your retreat config, schedule, checklist, journal, meal logs, precept logs, meditation sessions, and downloaded talks.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetRetreat()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        RetreatAppBar(title = "Retreat Preparation")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.config?.let { config ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Retreat Plan",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        config.startDate?.let {
                            Text(
                                text = "Start: ${TimeUtils.formatFullDate(it)}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        config.endDate?.let {
                            Text(
                                text = "End: ${TimeUtils.formatFullDate(it)}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Status: ${formatPhase(config.phase)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(onClick = { showEditDates = true }) {
                            Text("Edit Dates")
                        }
                    }
                }

                if (showEditDates) {
                    RetreatDatesDialog(
                        initialStart = config.startDate,
                        initialEnd = config.endDate,
                        confirmLabel = "Save",
                        onConfirm = { start, end ->
                            viewModel.updateRetreatDates(start, end)
                            showEditDates = false
                        },
                        onDismiss = { showEditDates = false }
                    )
                }

                // Checklist progress
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onChecklist
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Preparation Checklist",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val total = state.checklistItems.size
                        val completed = state.checklistItems.count { it.completed }
                        Text(
                            text = "$completed of $total items completed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Schedule
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onScheduleBuilder
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Daily Schedule",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (state.hasSchedule)
                                "Schedule configured"
                            else
                                "No schedule yet — tap to build",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Chants
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onChantCatalog
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Chants",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val chants = state.talks.filter { it.category == "Chants" }
                        val totalChants = chants.size
                        val downloaded = chants.count { it.downloadStatus == com.soloretreat.data.model.DownloadStatus.COMPLETE }
                        Text(
                            text = "$downloaded of $totalChants chants downloaded",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Talks
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onTalkCatalog
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Dhamma Talks",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val talks = state.talks.filter { it.category != "Chants" }
                        val totalTalks = talks.size
                        val downloaded = talks.count { it.downloadStatus == com.soloretreat.data.model.DownloadStatus.COMPLETE }
                        Text(
                            text = "$downloaded of $totalTalks talks downloaded",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Eight Precepts
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onEightPrecepts
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Eight Precepts",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Review the precepts observed during retreat",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (config.phase == Phase.READY) {
                    Button(
                        onClick = {
                            viewModel.startRetreat()
                            onStartRetreat()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Begin Retreat")
                    }
                } else if (config.phase == Phase.PREPARING) {
                    val checklistComplete = state.checklistItems.all { !it.required || it.completed }
                    val canStart = checklistComplete && state.hasSchedule

                    OutlinedButton(
                        onClick = {
                            viewModel.startRetreat()
                            onStartRetreat()
                        },
                        enabled = canStart,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            when {
                                !state.hasSchedule -> "Add a schedule block to begin"
                                !checklistComplete -> "Complete required checklist items to begin"
                                else -> "Begin Retreat"
                            }
                        )
                    }
                }

                if (config.phase == Phase.IN_PROGRESS) {
                    OutlinedButton(
                        onClick = onStartRetreat,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Return to Retreat")
                    }
                }

                TextButton(
                    onClick = { showResetConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset Retreat Data")
                }
            } ?: run {
                // No retreat configured
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Welcome",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Plan your solo meditation retreat with guided preparation, structured practice, and integration support.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    CreateRetreatDialog(
                        onCreate = { start, end ->
                            viewModel.createRetreat(start, end)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun formatPhase(phase: Phase): String = when (phase) {
    Phase.NOT_STARTED -> "Not Started"
    Phase.PREPARING -> "Preparing"
    Phase.READY -> "Ready to Begin"
    Phase.IN_PROGRESS -> "In Progress"
    Phase.COMPLETED -> "Completed"
}