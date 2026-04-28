package com.soloretreat.ui.integration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soloretreat.data.local.entity.RetreatConfig
import com.soloretreat.data.model.ActivityType
import com.soloretreat.data.repository.JournalRepository
import com.soloretreat.data.repository.MealLogRepository
import com.soloretreat.data.repository.MeditationSessionRepository
import com.soloretreat.data.repository.PreceptRepository
import com.soloretreat.data.repository.RetreatRepository
import com.soloretreat.util.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class RetreatSummaryViewModel @Inject constructor(
    private val retreatRepository: RetreatRepository,
    private val meditationSessionRepository: MeditationSessionRepository,
    private val preceptRepository: PreceptRepository,
    private val mealLogRepository: MealLogRepository,
    private val journalRepository: JournalRepository
) : ViewModel() {

    data class ActivityStat(
        val type: ActivityType,
        val displayName: String,
        val minutes: Long,
        val formattedTime: String,
        val completed: Int,
        val interrupted: Int
    )

    data class SummaryState(
        val config: RetreatConfig? = null,
        val sittingMinutes: Long = 0,
        val formattedSittingTime: String = "0m",
        val sittingCompleted: Int = 0,
        val sittingInterrupted: Int = 0,
        val walkingMinutes: Long = 0,
        val formattedWalkingTime: String = "0m",
        val walkingCompleted: Int = 0,
        val walkingInterrupted: Int = 0,
        val otherActivities: List<ActivityStat> = emptyList(),
        val preceptRate: Float = 0f,
        val onTimeMeals: Int = 0,
        val lateMeals: Int = 0,
        val isLoading: Boolean = true
    )

    private val _state = MutableStateFlow(SummaryState())
    val state: StateFlow<SummaryState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val config = retreatRepository.getConfig().firstOrNull()
            _state.value = _state.value.copy(config = config)

            if (config != null) {
                val startDate = config.startDate
                val endDate = config.endDate

                if (startDate != null && endDate != null) {
                    val startInstant = TimeUtils.localDateTimeToInstant(startDate, LocalTime.MIN)
                    val endInstant = TimeUtils.localDateTimeToInstant(endDate.plusDays(1), LocalTime.MIN)

                    val sittingMinutes = meditationSessionRepository.getTotalMinutesByType(ActivityType.SITTING, startInstant, endInstant)
                    val sittingCompleted = meditationSessionRepository.countCompletedByType(ActivityType.SITTING, startInstant, endInstant)
                    val sittingInterrupted = meditationSessionRepository.countInterruptedByType(ActivityType.SITTING, startInstant, endInstant)

                    val otherTypes = ActivityType.values().filter { it != ActivityType.SITTING && it != ActivityType.WALKING }
                    val otherStats = otherTypes.mapNotNull { type ->
                        val mins = meditationSessionRepository.getTotalMinutesByType(type, startInstant, endInstant)
                        val comp = meditationSessionRepository.countCompletedByType(type, startInstant, endInstant)
                        val intr = meditationSessionRepository.countInterruptedByType(type, startInstant, endInstant)
                        if (comp + intr == 0) null
                        else ActivityStat(type, type.displayName, mins, TimeUtils.formatDuration(mins), comp, intr)
                    }
                    val walkingMinutes = meditationSessionRepository.getTotalMinutesByType(ActivityType.WALKING, startInstant, endInstant)
                    val walkingCompleted = meditationSessionRepository.countCompletedByType(ActivityType.WALKING, startInstant, endInstant)
                    val walkingInterrupted = meditationSessionRepository.countInterruptedByType(ActivityType.WALKING, startInstant, endInstant)
                    val preceptRate = preceptRepository.getObservanceRate(startDate, endDate)
                    val onTime = mealLogRepository.countOnTimeMeals(startDate, endDate)
                    val late = mealLogRepository.countLateMeals(startDate, endDate)

                    _state.value = SummaryState(
                        config = config,
                        sittingMinutes = sittingMinutes,
                        formattedSittingTime = TimeUtils.formatDuration(sittingMinutes),
                        sittingCompleted = sittingCompleted,
                        sittingInterrupted = sittingInterrupted,
                        walkingMinutes = walkingMinutes,
                        formattedWalkingTime = TimeUtils.formatDuration(walkingMinutes),
                        walkingCompleted = walkingCompleted,
                        walkingInterrupted = walkingInterrupted,
                        otherActivities = otherStats,
                        preceptRate = preceptRate,
                        onTimeMeals = onTime,
                        lateMeals = late,
                        isLoading = false
                    )
                }
            }
        }
    }

    suspend fun generateExportText(): String {
        val s = _state.value
        val config = s.config ?: return ""
        val startDateStr = config.startDate?.let { TimeUtils.formatFullDate(it) } ?: "?"
        val endDateStr = config.endDate?.let { TimeUtils.formatFullDate(it) } ?: "?"

        val journalEntries = if (config.startDate != null && config.endDate != null) {
            val startInstant = TimeUtils.localDateTimeToInstant(config.startDate, LocalTime.MIN)
            val endInstant = TimeUtils.localDateTimeToInstant(config.endDate.plusDays(1), LocalTime.MIN)
            journalRepository.getEntriesForRange(startInstant, endInstant)
        } else {
            emptyList()
        }

        return buildString {
            appendLine("SOLO RETREAT SUMMARY")
            appendLine("====================")
            appendLine()
            appendLine("Dates: $startDateStr to $endDateStr")
            appendLine()
            appendLine("SITTING MEDITATION")
            appendLine("  Total time: ${s.formattedSittingTime}")
            appendLine("  Completed sessions: ${s.sittingCompleted}")
            appendLine("  Interrupted sessions: ${s.sittingInterrupted}")
            appendLine()
            appendLine("WALKING MEDITATION")
            appendLine("  Total time: ${s.formattedWalkingTime}")
            appendLine("  Completed sessions: ${s.walkingCompleted}")
            appendLine("  Interrupted sessions: ${s.walkingInterrupted}")
            appendLine()
            s.otherActivities.forEach { stat ->
                appendLine(stat.displayName.uppercase())
                appendLine("  Total time: ${stat.formattedTime}")
                appendLine("  Completed sessions: ${stat.completed}")
                appendLine("  Interrupted sessions: ${stat.interrupted}")
                appendLine()
            }
            appendLine("PRECEPTS")
            appendLine("  Average observance: ${(s.preceptRate * 100).toInt()}%")
            appendLine()
            appendLine("MEALS")
            appendLine("  Before noon: ${s.onTimeMeals}")
            appendLine("  After noon: ${s.lateMeals}")
            appendLine()
            
            if (journalEntries.isNotEmpty()) {
                appendLine("JOURNAL ENTRIES")
                appendLine("---------------")
                journalEntries.forEach { entry ->
                    appendLine("[${TimeUtils.formatDateTime(entry.createdAt)}]")
                    if (entry.tags.isNotBlank()) {
                        appendLine("Tags: ${entry.tags}")
                    }
                    appendLine(entry.entryText)
                    appendLine()
                }
            }

            appendLine("Generated by SoloRetreat")
        }
    }
}
