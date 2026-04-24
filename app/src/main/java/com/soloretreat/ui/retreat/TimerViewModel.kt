package com.soloretreat.ui.retreat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.soloretreat.data.model.ActivityType
import com.soloretreat.service.TimerEngine
import com.soloretreat.util.TimerFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val engine: TimerEngine,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialActivityType: String? = savedStateHandle["activityType"]

    val state: StateFlow<TimerEngine.TimerState> = engine.state

    init {
        val cur = engine.state.value
        if (cur.isComplete) engine.abandon()
        engine.setActivityLabel(getActivityLabel(initialActivityType))
        engine.setActivityType(parseActivityType(initialActivityType))
    }

    private fun parseActivityType(type: String?): ActivityType? {
        return try {
            if (type == null) ActivityType.SITTING
            else ActivityType.valueOf(type)
        } catch (_: Exception) {
            ActivityType.SITTING
        }
    }

    fun selectDuration(minutes: Int) = engine.selectDuration(minutes)
    fun startTimer() = engine.start()
    fun togglePause() = engine.togglePause()
    fun abandonSession() = engine.abandon()

    private fun getActivityLabel(type: String?): String {
        return try {
            if (type == null) "Sitting Meditation"
            else when (val activity = ActivityType.valueOf(type)) {
                ActivityType.SITTING, ActivityType.WALKING -> activity.displayName
                else -> "Sitting Meditation"
            }
        } catch (_: Exception) {
            "Sitting Meditation"
        }
    }

    companion object {
        fun formatSeconds(totalSeconds: Long): String = TimerFormatter.formatSeconds(totalSeconds)
    }
}
