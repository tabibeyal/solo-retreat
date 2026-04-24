package com.soloretreat.service

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import com.soloretreat.data.local.entity.MeditationSession
import com.soloretreat.data.model.ActivityType
import com.soloretreat.data.repository.MeditationSessionRepository
import com.soloretreat.util.TimerFormatter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerEngine @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val bellManager: BellManager,
    private val sessionRepository: MeditationSessionRepository
) {
    data class TimerState(
        val activityLabel: String = "Sitting Meditation",
        val selectedMinutes: Int = 30,
        val elapsedSeconds: Long = 0,
        val remainingSeconds: Long = 30 * 60L,
        val isRunning: Boolean = false,
        val isPaused: Boolean = false,
        val isComplete: Boolean = false
    ) {
        val formattedTime: String
            get() = TimerFormatter.formatSeconds(
                if (isRunning || isPaused || isComplete) remainingSeconds
                else selectedMinutes * 60L
            )
    }

    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var tickJob: Job? = null

    private var totalMs: Long = 0
    private var accumulatedMs: Long = 0
    private var segmentStartRealtime: Long = 0

    private var currentActivityType: ActivityType? = null
    private var currentSessionId: String? = null

    fun setActivityLabel(label: String) {
        val cur = _state.value
        if (!cur.isRunning && !cur.isPaused) {
            _state.value = cur.copy(activityLabel = label)
        }
    }

    fun setActivityType(type: ActivityType?) {
        val cur = _state.value
        if (!cur.isRunning && !cur.isPaused) {
            currentActivityType = type
        }
    }

    fun selectDuration(minutes: Int) {
        val cur = _state.value
        if (!cur.isRunning && !cur.isPaused) {
            _state.value = cur.copy(
                selectedMinutes = minutes,
                remainingSeconds = minutes * 60L,
                elapsedSeconds = 0,
                isComplete = false
            )
        }
    }

    fun start() {
        val cur = _state.value
        totalMs = cur.selectedMinutes * 60_000L
        accumulatedMs = 0L
        segmentStartRealtime = SystemClock.elapsedRealtime()
        bellManager.playStartBell()
        _state.value = cur.copy(
            elapsedSeconds = 0,
            remainingSeconds = cur.selectedMinutes * 60L,
            isRunning = true,
            isPaused = false,
            isComplete = false
        )
        logSessionStart()
        startService()
        startTicking()
    }

    fun togglePause() {
        val cur = _state.value
        when {
            cur.isRunning -> {
                accumulatedMs += SystemClock.elapsedRealtime() - segmentStartRealtime
                tickJob?.cancel()
                _state.value = cur.copy(isRunning = false, isPaused = true)
            }
            cur.isPaused -> {
                segmentStartRealtime = SystemClock.elapsedRealtime()
                _state.value = cur.copy(isRunning = true, isPaused = false)
                startTicking()
            }
        }
    }

    fun abandon() {
        tickJob?.cancel()
        bellManager.release()
        logSessionEnd(interrupted = true)
        totalMs = 0
        accumulatedMs = 0
        val cur = _state.value
        _state.value = cur.copy(
            isRunning = false,
            isPaused = false,
            isComplete = false,
            remainingSeconds = cur.selectedMinutes * 60L,
            elapsedSeconds = 0
        )
        stopService()
    }

    private fun startTicking() {
        tickJob?.cancel()
        tickJob = scope.launch {
            while (isActive) {
                val cur = _state.value
                if (!cur.isRunning) break
                val elapsedMs = accumulatedMs + (SystemClock.elapsedRealtime() - segmentStartRealtime)
                val remainingMs = (totalMs - elapsedMs).coerceAtLeast(0)
                _state.update {
                    it.copy(
                        elapsedSeconds = elapsedMs / 1000,
                        remainingSeconds = remainingMs / 1000
                    )
                }
                if (remainingMs <= 0) {
                    complete()
                    break
                }
                delay(250)
            }
        }
    }

    private fun complete() {
        bellManager.playEndBell()
        logSessionEnd(interrupted = false)
        _state.update {
            it.copy(
                isRunning = false,
                isPaused = false,
                isComplete = true,
                remainingSeconds = 0
            )
        }
        stopService()
    }

    private fun logSessionStart() {
        val type = currentActivityType
        if (type != ActivityType.SITTING && type != ActivityType.WALKING) {
            currentSessionId = null
            return
        }
        val id = UUID.randomUUID().toString()
        currentSessionId = id
        val session = MeditationSession(
            id = id,
            blockId = "",
            actualStart = Instant.now(),
            actualEnd = null,
            interrupted = false,
            activityType = type
        )
        scope.launch { sessionRepository.insert(session) }
    }

    private fun logSessionEnd(interrupted: Boolean) {
        val id = currentSessionId ?: return
        currentSessionId = null
        scope.launch {
            sessionRepository.completeSession(id, Instant.now(), interrupted)
        }
    }

    private fun startService() {
        val intent = Intent(context, MeditationTimerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun stopService() {
        context.stopService(Intent(context, MeditationTimerService::class.java))
    }
}
