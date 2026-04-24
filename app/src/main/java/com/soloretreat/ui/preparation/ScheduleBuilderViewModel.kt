package com.soloretreat.ui.preparation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soloretreat.data.local.entity.ScheduleBlock
import com.soloretreat.data.local.entity.ScheduleTemplate
import com.soloretreat.data.model.ActivityType
import com.soloretreat.data.repository.RetreatRepository
import com.soloretreat.data.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class ScheduleBuilderViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    retreatRepository: RetreatRepository
) : ViewModel() {

    var totalDays: Int = 3
        private set

    val blocks: StateFlow<List<ScheduleBlock>> = scheduleRepository.getAllBlocks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val templates: StateFlow<List<ScheduleTemplate>> = scheduleRepository.getTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _validationResult = MutableStateFlow<ScheduleRepository.ScheduleValidationResult?>(null)
    val validationResult: StateFlow<ScheduleRepository.ScheduleValidationResult?> = _validationResult

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _info = MutableStateFlow<String?>(null)
    val info: StateFlow<String?> = _info

    init {
        viewModelScope.launch {
            val config = retreatRepository.getConfigSync()
            config?.startDate?.let { start ->
                config.endDate?.let { end ->
                    totalDays = java.time.Duration.between(
                        start.atStartOfDay(),
                        end.plusDays(1).atStartOfDay()
                    ).toDays().toInt()
                }
            }
        }
        validateSchedule()
    }

    fun addBlock(dayOffset: Int, activityType: ActivityType, startTime: LocalTime, endTime: LocalTime, notes: String?) {
        viewModelScope.launch {
            val block = ScheduleBlock(
                dayOffset = dayOffset,
                startTime = startTime,
                endTime = endTime,
                activityType = activityType,
                notes = notes
            )
            val success = scheduleRepository.addBlock(block)
            if (success) {
                _error.value = null
                validateSchedule()
            } else {
                _error.value = "Cannot add block: Overlap with existing block or invalid time range."
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearInfo() {
        _info.value = null
    }

    fun removeBlock(block: ScheduleBlock) {
        viewModelScope.launch {
            scheduleRepository.deleteBlock(block)
            validateSchedule()
        }
    }

    fun saveTemplate(name: String) {
        viewModelScope.launch {
            val ok = scheduleRepository.saveCurrentAsTemplate(name)
            if (ok) {
                _info.value = "Template saved"
            } else {
                _error.value = "Cannot save: name empty or schedule has no blocks"
            }
        }
    }

    fun applyTemplate(templateId: String) {
        viewModelScope.launch {
            scheduleRepository.applyTemplate(templateId)
            validateSchedule()
            _info.value = "Template applied"
        }
    }

    fun deleteTemplate(templateId: String) {
        viewModelScope.launch {
            scheduleRepository.deleteTemplate(templateId)
        }
    }

    fun getLastEndTime(dayOffset: Int): LocalTime {
        return blocks.value
            .filter { it.dayOffset == dayOffset }
            .maxByOrNull { it.endTime }
            ?.endTime ?: LocalTime.of(6, 0)
    }

    private fun validateSchedule() {
        viewModelScope.launch {
            _validationResult.value = scheduleRepository.validateSchedule()
        }
    }
}
