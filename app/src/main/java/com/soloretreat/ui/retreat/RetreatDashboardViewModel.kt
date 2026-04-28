package com.soloretreat.ui.retreat

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soloretreat.data.local.entity.RetreatConfig
import com.soloretreat.data.local.entity.ScheduleBlock
import com.soloretreat.data.model.Phase
import com.soloretreat.data.repository.RetreatRepository
import com.soloretreat.data.repository.ScheduleRepository
import com.soloretreat.service.RetreatAlarmScheduler
import com.soloretreat.service.TimerEngine
import com.soloretreat.util.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class RetreatDashboardViewModel @Inject constructor(
    private val retreatRepository: RetreatRepository,
    private val scheduleRepository: ScheduleRepository,
    private val alarmScheduler: RetreatAlarmScheduler,
    private val timerEngine: TimerEngine,
    @ApplicationContext private val context: Context
) : ViewModel() {

    data class DashboardState(
        val config: RetreatConfig? = null,
        val currentBlock: ScheduleBlock? = null,
        val nextBlock: ScheduleBlock? = null,
        val showMealCountdown: Boolean = true,
        val mealCutoffApproaching: Boolean = false
    )

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            retreatRepository.getConfig().collect { config ->
                _state.value = _state.value.copy(config = config)
                if (config != null) {
                    refreshBlocks()
                }
            }
        }

        viewModelScope.launch {
            while (isActive) {
                refreshBlocks()
                _state.value = _state.value.copy(
                    mealCutoffApproaching = TimeUtils.isMealCutoffApproaching()
                )
                delay(30000)
            }
        }
    }

    fun endRetreat(onComplete: () -> Unit) {
        viewModelScope.launch {
            if (timerEngine.state.value.isRunning || timerEngine.state.value.isPaused) {
                timerEngine.abandon()
            }
            retreatRepository.updatePhase(Phase.COMPLETED)
            alarmScheduler.cancelAll()
            updateWidget()
            onComplete()
        }
    }

    private fun updateWidget() {
        viewModelScope.launch {
            com.soloretreat.ui.widget.RetreatWidget().updateAll(context)
        }
    }

    private suspend fun refreshBlocks() {
        val config = _state.value.config ?: return
        val startDate = config.startDate ?: return
        val now = LocalTime.now()
        val dayOffset = TimeUtils.dayOfRetreat(startDate) - 1

        val current = scheduleRepository.getCurrentBlock(dayOffset, now)
        val next = if (current == null) {
            scheduleRepository.getNextBlock(dayOffset, now)
        } else null

        _state.value = _state.value.copy(
            currentBlock = current,
            nextBlock = next
        )
    }
}