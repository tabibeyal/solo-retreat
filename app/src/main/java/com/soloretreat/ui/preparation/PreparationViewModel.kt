package com.soloretreat.ui.preparation

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soloretreat.data.local.entity.ChecklistItem
import com.soloretreat.data.local.entity.DhammaTalk
import com.soloretreat.data.local.entity.RetreatConfig
import com.soloretreat.data.model.Phase
import com.soloretreat.data.repository.RetreatRepository
import com.soloretreat.data.repository.ScheduleRepository
import com.soloretreat.data.repository.TalkRepository
import com.soloretreat.service.RetreatAlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PreparationViewModel @Inject constructor(
    private val retreatRepository: RetreatRepository,
    private val scheduleRepository: ScheduleRepository,
    private val talkRepository: TalkRepository,
    private val alarmScheduler: RetreatAlarmScheduler,
    @ApplicationContext private val context: Context
) : ViewModel() {

    data class PreparationState(
        val config: RetreatConfig? = null,
        val checklistItems: List<ChecklistItem> = emptyList(),
        val talks: List<DhammaTalk> = emptyList(),
        val hasSchedule: Boolean = false,
        val isLoading: Boolean = true
    )

    private val _state = MutableStateFlow(PreparationState())
    val state: StateFlow<PreparationState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            talkRepository.importCatalogFromAssets()
        }

        viewModelScope.launch {
            combine(
                retreatRepository.getConfig(),
                retreatRepository.getChecklistForPhase(Phase.PREPARING),
                talkRepository.getAllTalks(),
                scheduleRepository.getAllBlocks()
            ) { config, checklist, talks, blocks ->
                PreparationState(
                    config = config,
                    checklistItems = checklist,
                    talks = talks,
                    hasSchedule = blocks.isNotEmpty(),
                    isLoading = false
                )
            }.collect { state ->
                _state.value = state
            }
        }
    }

    fun createRetreat(startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            retreatRepository.createRetreat(startDate, endDate)
        }
    }

    fun updateRetreatDates(startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            val config = retreatRepository.getConfig().firstOrNull() ?: return@launch
            val updated = config.copy(startDate = startDate, endDate = endDate)
            retreatRepository.updateConfig(updated)
            if (updated.phase == Phase.READY || updated.phase == Phase.IN_PROGRESS) {
                alarmScheduler.scheduleForRetreat(updated)
            }
            updateWidget()
        }
    }

    fun startRetreat() {
        viewModelScope.launch {
            retreatRepository.updatePhase(Phase.IN_PROGRESS)
            val config = retreatRepository.getConfig().firstOrNull() ?: return@launch
            alarmScheduler.scheduleForRetreat(config)
            updateWidget()
        }
    }

    fun resetRetreat() {
        viewModelScope.launch {
            alarmScheduler.cancelAll()
            retreatRepository.resetRetreat()
            updateWidget()
        }
    }

    private fun updateWidget() {
        viewModelScope.launch {
            com.soloretreat.ui.widget.RetreatWidget().updateAll(context)
        }
    }
}
