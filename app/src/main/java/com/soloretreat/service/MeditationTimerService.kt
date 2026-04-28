package com.soloretreat.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.soloretreat.util.Constants
import com.soloretreat.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MeditationTimerService : Service() {

    @Inject lateinit var timerEngine: TimerEngine
    @Inject lateinit var notificationHelper: NotificationHelper

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var observerJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        val initial = timerEngine.state.value
        val notification = notificationHelper.buildTimerNotification(
            activityLabel = initial.activityLabel,
            remainingSeconds = initial.remainingSeconds,
            isPaused = initial.isPaused
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                Constants.NOTIFICATION_ID_TIMER,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(Constants.NOTIFICATION_ID_TIMER, notification)
        }
        observerJob = scope.launch {
            timerEngine.state
                .map { Triple(it.activityLabel, it.remainingSeconds, it.isRunning to it.isPaused) }
                .distinctUntilChanged()
                .collect { _ ->
                    val s = timerEngine.state.value
                    if (!s.isRunning && !s.isPaused) {
                        stopSelf()
                        return@collect
                    }
                    val updated = notificationHelper.buildTimerNotification(
                        activityLabel = s.activityLabel,
                        remainingSeconds = s.remainingSeconds,
                        isPaused = s.isPaused
                    )
                    notificationHelper.notifyTimer(updated)
                }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == Constants.ACTION_STOP_TIMER) {
            scope.launch {
                timerEngine.abandon()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        observerJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }
}
