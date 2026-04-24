package com.soloretreat.ui.preparation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("AssignedValueIsNeverRead")
fun RetreatDatesDialog(
    initialStart: LocalDate?,
    initialEnd: LocalDate?,
    confirmLabel: String,
    onConfirm: (LocalDate, LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    val currentStart = startDate

    if (currentStart == null) {
        val initialMillis = initialStart
            ?.atStartOfDay(ZoneId.systemDefault())
            ?.toInstant()
            ?.toEpochMilli()
            ?: System.currentTimeMillis()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                Button(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        startDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                }) {
                    Text("Next")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(4.dp)
        ) {
            DatePicker(state = datePickerState, title = { Text("Select Start Date") })
        }
    } else {
        val initialEndMillis = (initialEnd ?: currentStart.plusDays(1))
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialEndMillis)
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                Button(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val end = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onConfirm(currentStart, end)
                    }
                }) {
                    Text(confirmLabel)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { startDate = null }) {
                    Text("Back")
                }
            },
            shape = RoundedCornerShape(4.dp)
        ) {
            DatePicker(state = datePickerState, title = { Text("Select End Date") })
        }
    }
}

@Composable
fun CreateRetreatDialog(
    onCreate: (LocalDate, LocalDate) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Button(
        onClick = { showDialog = true },
        modifier = Modifier.fillMaxWidth(0.7f)
    ) {
        Text("Create Retreat Plan")
    }

    if (showDialog) {
        RetreatDatesDialog(
            initialStart = null,
            initialEnd = null,
            confirmLabel = "Create Retreat",
            onConfirm = { start, end ->
                onCreate(start, end)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}
