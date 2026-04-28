package com.soloretreat.ui.retreat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soloretreat.ui.theme.TimerBackground
import com.soloretreat.ui.theme.TimerDisplayStyle
import com.soloretreat.ui.theme.TimerText

@Composable
fun TimerScreen(
    onNavigateBack: () -> Unit,
    viewModel: TimerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    KeepScreenOn()

    LaunchedEffect(state.lastLogEvent?.id) {
        val event = state.lastLogEvent ?: return@LaunchedEffect
        val label = if (event.interrupted)
            "Stopped early — logged ${event.elapsedMinutes}m"
        else
            "Session complete — logged ${event.elapsedMinutes}m"
        val result = snackbarHostState.showSnackbar(
            message = label,
            actionLabel = "Discard",
            duration = SnackbarDuration.Short
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.discardLastLog()
        } else {
            viewModel.consumeLogEvent()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TimerBackground)
            .systemBarsPadding()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Activity label
            Text(
                text = state.activityLabel,
                style = MaterialTheme.typography.titleLarge,
                color = TimerText.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Timer display
            Text(
                text = state.formattedTime,
                style = TimerDisplayStyle.copy(color = TimerText),
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                overflow = androidx.compose.ui.text.style.TextOverflow.Visible
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (state.isRunning) "In Progress" else if (state.isPaused) "Paused" else "Ready",
                style = MaterialTheme.typography.bodyLarge,
                color = TimerText.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Controls
            if (!state.isRunning && !state.isPaused) {
                DurationSelector(
                    selectedMinutes = state.selectedMinutes,
                    onDurationSelected = { viewModel.selectDuration(it) }
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.startTimer() },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Begin Session")
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { viewModel.togglePause() },
                        modifier = Modifier
                            .width(80.dp)
                            .height(80.dp)
                    ) {
                        Icon(
                            imageVector = if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (state.isPaused) "Resume" else "Pause",
                            tint = TimerText,
                            modifier = Modifier.fillMaxSize(0.6f)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.abandonSession() },
                        modifier = Modifier
                            .width(80.dp)
                            .height(80.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Abandon",
                            tint = Color(0xFFE57373),
                            modifier = Modifier.fillMaxSize(0.6f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (state.remainingSeconds == 0L) {
                    Text(
                        "Session Complete",
                        color = TimerText.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            viewModel.abandonSession()
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TimerText
                        )
                    ) {
                        Text("Return to Dashboard")
                    }
                }
            }
        }

        // Back button in corner — leave timer running in background
        IconButton(
            onClick = { onNavigateBack() },
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = TimerText.copy(alpha = 0.5f)
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun DurationSelector(
    selectedMinutes: Int,
    onDurationSelected: (Int) -> Unit
) {
    val durations = listOf(15, 30, 45, 60, 90)
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        durations.forEach { mins ->
            val selected = mins == selectedMinutes
            androidx.compose.material3.Surface(
                onClick = { onDurationSelected(mins) },
                shape = androidx.compose.foundation.shape.CircleShape,
                color = if (selected)
                    MaterialTheme.colorScheme.primary
                else
                    TimerText.copy(alpha = 0.15f),
                contentColor = if (selected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    TimerText,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${mins}m",
                    modifier = Modifier.padding(vertical = 12.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}