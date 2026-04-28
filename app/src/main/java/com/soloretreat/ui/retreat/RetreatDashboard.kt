package com.soloretreat.ui.retreat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soloretreat.ui.components.CountdownDisplay
import com.soloretreat.ui.components.RetreatAppBar
import com.soloretreat.util.TimeUtils
import java.time.LocalTime

@Composable
@Suppress("SpellCheckingInspection")
fun RetreatDashboardScreen(
    onTimer: (String?) -> Unit,
    onPrecepts: () -> Unit,
    onMeal: () -> Unit,
    onJournal: () -> Unit,
    onSchedule: () -> Unit,
    onChants: () -> Unit,
    onTalks: () -> Unit,
    onEndRetreat: () -> Unit,
    viewModel: RetreatDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            RetreatAppBar(
                title = "Retreat Dashboard",
                actions = {}
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Day counter
            state.config?.let { config ->
                val startDate = config.startDate
                val endDate = config.endDate
                if (startDate != null && endDate != null) {
                    val dayOfRetreat = TimeUtils.dayOfRetreat(startDate)
                    val totalDays = TimeUtils.daysBetween(startDate, endDate)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Day $dayOfRetreat of $totalDays",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = TimeUtils.formatFullDate(java.time.LocalDate.now()),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Current / Next block
            val currentBlock = state.currentBlock
            val nextBlock = state.nextBlock

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSchedule() },
                colors = CardDefaults.cardColors(
                    containerColor = if (currentBlock != null)
                        MaterialTheme.colorScheme.secondaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (currentBlock != null) {
                        Text(
                            text = "Current Block",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentBlock.activityType.displayName,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "${TimeUtils.formatTime(currentBlock.startTime)} - ${TimeUtils.formatTime(currentBlock.endTime)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else if (nextBlock != null) {
                        Text(
                            text = "Next: ${nextBlock.activityType.displayName}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        CountdownDisplay(
                            targetTime = nextBlock.startTime,
                            label = "Starts in",
                            timerStyle = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontFeatureSettings = "tnum"
                            )
                        )
                    } else {
                        Text(
                            text = "No more blocks today",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap to view full schedule",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Meal cutoff countdown
            if (state.showMealCountdown) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.mealCutoffApproaching)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Meal Cutoff",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        CountdownDisplay(
                            targetTime = LocalTime.of(12, 0),
                            label = "Time until noon",
                            timerStyle = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontFeatureSettings = "tnum"
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick actions grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton(
                    icon = Icons.Default.Timer,
                    label = "Timer",
                    onClick = { onTimer(state.currentBlock?.activityType?.name) }
                )
                QuickActionButton(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    label = "Precepts",
                    onClick = onPrecepts
                )
                QuickActionButton(
                    icon = Icons.Default.Fastfood,
                    label = "Meal Log",
                    onClick = onMeal
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton(
                    icon = Icons.Default.MusicNote,
                    label = "Chants",
                    onClick = onChants
                )
                QuickActionButton(
                    icon = Icons.Default.Headphones,
                    label = "Talks",
                    onClick = onTalks
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(onClick = onJournal) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Journal")
                }
                OutlinedButton(onClick = {
                    viewModel.endRetreat {
                        onEndRetreat()
                    }
                }) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("End Retreat")
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = onClick,
            modifier = Modifier.width(80.dp)
        ) {
            Icon(icon, contentDescription = label)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
    }
}