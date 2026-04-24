package com.soloretreat.ui.preparation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soloretreat.data.local.entity.ScheduleTemplate
import com.soloretreat.data.model.ActivityType
import com.soloretreat.ui.components.RetreatAppBar
import com.soloretreat.ui.components.ScheduleBlockItem
import com.soloretreat.util.TimeUtils
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("AssignedValueIsNeverRead", "SpellCheckingInspection")
fun ScheduleBuilderScreen(
    onNavigateBack: () -> Unit,
    viewModel: ScheduleBuilderViewModel = hiltViewModel()
) {
    val blocks by viewModel.blocks.collectAsState()
    val templates by viewModel.templates.collectAsState()
    val validationResult by viewModel.validationResult.collectAsState()
    val error by viewModel.error.collectAsState()
    val info by viewModel.info.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showOverflow by remember { mutableStateOf(false) }
    var showSaveTemplateDialog by remember { mutableStateOf(false) }
    var showLoadTemplateDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(info) {
        info?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearInfo()
        }
    }

    Scaffold(
        topBar = {
            RetreatAppBar(
                title = "Daily Schedule",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { showOverflow = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showOverflow,
                        onDismissRequest = { showOverflow = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Save as Template") },
                            onClick = {
                                showOverflow = false
                                showSaveTemplateDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Load Template") },
                            onClick = {
                                showOverflow = false
                                showLoadTemplateDialog = true
                            }
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add block")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
            .padding(padding)
        .padding(horizontal = 16.dp)
        ) {
            validationResult?.let { result ->
                when (result) {
                    is com.soloretreat.data.repository.ScheduleRepository.ScheduleValidationResult.Valid -> {
                        Text(
                            "Schedule is valid",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    is com.soloretreat.data.repository.ScheduleRepository.ScheduleValidationResult.NoBlocks -> {
                        Text(
                            "Add schedule blocks for your retreat days",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    is com.soloretreat.data.repository.ScheduleRepository.ScheduleValidationResult.Overlap -> {
                        Text(
                            "Overlap on day ${result.dayOffset + 1}: ${result.block1} overlaps ${result.block2}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    is com.soloretreat.data.repository.ScheduleRepository.ScheduleValidationResult.MealTooLate -> {
                        Text(
                            "Meal on day ${result.dayOffset + 1} ends at ${TimeUtils.formatTime(result.endTime)}, which is after noon",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(blocks) { block ->
                    ScheduleBlockItem(
                        block = block,
                        onRemove = { viewModel.removeBlock(block) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddBlockDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { dayOffset, activityType, startTime, endTime, notes ->
                viewModel.addBlock(dayOffset, activityType, startTime, endTime, notes)
                showAddDialog = false
            },
            totalDays = viewModel.totalDays,
            onGetLastEndTime = { day -> viewModel.getLastEndTime(day) }
        )
    }

    if (showSaveTemplateDialog) {
        SaveTemplateDialog(
            onDismiss = { showSaveTemplateDialog = false },
            onSave = { name ->
                viewModel.saveTemplate(name)
                showSaveTemplateDialog = false
            }
        )
    }

    if (showLoadTemplateDialog) {
        LoadTemplateDialog(
            templates = templates,
            onDismiss = { showLoadTemplateDialog = false },
            onApply = { id ->
                viewModel.applyTemplate(id)
                showLoadTemplateDialog = false
            },
            onDelete = { id -> viewModel.deleteTemplate(id) }
        )
    }
}

@Composable
private fun SaveTemplateDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Schedule as Template") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Template name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(name) },
                enabled = name.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoadTemplateDialog(
    templates: List<ScheduleTemplate>,
    onDismiss: () -> Unit,
    onApply: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Load Template") },
        text = {
            if (templates.isEmpty()) {
                Text("No saved templates yet.")
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    templates.forEach { template ->
                        ListItem(
                            headlineContent = { Text(template.name) },
                            trailingContent = {
                                IconButton(onClick = { onDelete(template.id) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete template",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            modifier = Modifier.clickable { onApply(template.id) }
                        )
                    }
                    Text(
                        "Loading a template replaces the current schedule.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("AssignedValueIsNeverRead")
private fun AddBlockDialog(
    onDismiss: () -> Unit,
    onAdd: (Int, ActivityType, LocalTime, LocalTime, String?) -> Unit,
    totalDays: Int,
    onGetLastEndTime: (Int) -> LocalTime
) {
    var selectedDay by remember { mutableIntStateOf(0) }
    var selectedActivity by remember { mutableStateOf(ActivityType.SITTING) }
    var expandedActivity by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    
    key(selectedDay) {
        val initialTime = onGetLastEndTime(selectedDay)
        val startTimeState = rememberTimePickerState(initialHour = initialTime.hour, initialMinute = initialTime.minute)
        val endTimeState = rememberTimePickerState(initialHour = initialTime.plusHours(1).hour, initialMinute = initialTime.plusHours(1).minute)

        var showError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Add Schedule Block") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (showError != null) {
                        Text(
                            text = showError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Day selector
                    if (totalDays > 1) {
                        Text("Day: ${selectedDay + 1} of $totalDays", style = MaterialTheme.typography.labelLarge)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TextButton(
                                onClick = { if (selectedDay > 0) selectedDay-- },
                                enabled = selectedDay > 0
                            ) { Text("-") }
                            Text(
                                "${selectedDay + 1}",
                                modifier = Modifier.padding(horizontal = 16.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            TextButton(
                                onClick = { if (selectedDay < totalDays - 1) selectedDay++ },
                                enabled = selectedDay < totalDays - 1
                            ) { Text("+") }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Activity type
                    ExposedDropdownMenuBox(
                        expanded = expandedActivity,
                        onExpandedChange = { expandedActivity = it }
                    ) {
                        OutlinedTextField(
                            value = selectedActivity.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Activity") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedActivity) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedActivity,
                            onDismissRequest = { expandedActivity = false }
                        ) {
                            ActivityType.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.displayName) },
                                    onClick = {
                                        selectedActivity = type
                                        expandedActivity = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Start Time", style = MaterialTheme.typography.labelLarge)
                    TimeInput(state = startTimeState)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("End Time", style = MaterialTheme.typography.labelLarge)
                    TimeInput(state = endTimeState)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val start = LocalTime.of(startTimeState.hour, startTimeState.minute)
                    val end = LocalTime.of(endTimeState.hour, endTimeState.minute)
                    if (!end.isAfter(start)) {
                        showError = "End time must be after start time"
                    } else {
                        onAdd(selectedDay, selectedActivity, start, end, notes.ifBlank { null })
                    }
                }) {
                    Text("Add Block")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}
