package com.soloretreat.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import com.soloretreat.data.model.ActivityType
import com.soloretreat.data.model.Phase
import com.soloretreat.data.repository.RetreatRepository
import com.soloretreat.data.repository.ScheduleRepository
import com.soloretreat.util.Constants
import com.soloretreat.util.NotificationHelper
import com.soloretreat.util.TimeUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@AndroidEntryPoint
class RetreatAlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var retreatRepository: RetreatRepository
    @Inject lateinit var scheduleRepository: ScheduleRepository
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var scheduler: RetreatAlarmScheduler

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        scope.launch {
            try {
                val config = retreatRepository.getConfig().firstOrNull()
                if (config?.phase != Phase.IN_PROGRESS) {
                    scheduler.cancelAll()
                    return@launch
                }
                val startDate = config.startDate ?: return@launch
                val endDate = config.endDate ?: return@launch

                when (intent.action) {
                    Constants.ACTION_BLOCK_ALARM -> {
                        handleBlockAlarm(context, startDate, endDate)
                        updateWidget(context)
                    }
                    Constants.ACTION_MEAL_CUTOFF_WARN -> handleMealWarn(context)
                    Constants.ACTION_MEAL_CUTOFF_POST_NOON -> handleMealPostNoon(context, startDate)
                    Constants.ACTION_DAILY_ROLLOVER -> {
                        handleDailyRollover()
                        updateWidget(context)
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun handleBlockAlarm(context: Context, startDate: java.time.LocalDate, endDate: java.time.LocalDate) {
        val dayOffset = TimeUtils.dayOfRetreat(startDate) - 1
        val now = LocalTime.now()
        val currentBlock = scheduleRepository.getCurrentBlock(dayOffset, now)

        if (currentBlock != null && shouldNotifyForBlock(currentBlock.activityType)) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(
                Constants.NOTIFICATION_ID_BLOCK_REMINDER,
                notificationHelper.buildBlockReminderNotification(
                    currentBlock.activityType,
                    currentBlock.activityType.displayName
                )
            )
            context.sendBroadcast(Intent(Constants.ACTION_BLOCK_TRANSITION).apply {
                putExtra("block_id", currentBlock.id)
                putExtra("activity_type", currentBlock.activityType.name)
            })
        }

        refreshOngoingNotification(context, startDate, endDate)
        scheduler.rescheduleNextBlock(startDate, endDate)
    }

    private fun handleMealWarn(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(
            Constants.NOTIFICATION_ID_MEAL_CUTOFF,
            notificationHelper.buildMealCutoffNotification()
        )
    }

    private suspend fun handleMealPostNoon(context: Context, startDate: java.time.LocalDate) {
        val dayOffset = TimeUtils.dayOfRetreat(startDate) - 1
        val blocks = scheduleRepository.getBlocksForDay(dayOffset)
        val hasMealBlock = blocks.any { it.activityType == ActivityType.MEAL }
        if (hasMealBlock) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(
                Constants.NOTIFICATION_ID_MEAL_CUTOFF,
                notificationHelper.buildPersistentMealReminder()
            )
        }
    }

    private suspend fun handleDailyRollover() {
        val config = retreatRepository.getConfigSync() ?: return
        scheduler.scheduleForRetreat(config)
    }

    private suspend fun refreshOngoingNotification(context: Context, startDate: java.time.LocalDate, endDate: java.time.LocalDate) {
        val dayOfRetreat = TimeUtils.dayOfRetreat(startDate)
        val totalDays = TimeUtils.daysBetween(startDate, endDate)
        val now = LocalTime.now()
        val dayOffset = dayOfRetreat - 1
        val nextBlock = scheduleRepository.getNextBlock(dayOffset, now)

        val notification = notificationHelper.buildOngoingRetreatNotification(
            dayOfRetreat.coerceAtLeast(1),
            totalDays.coerceAtLeast(1),
            nextBlock?.activityType?.displayName,
            nextBlock?.startTime?.let { TimeUtils.formatTime(it) }
        )
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(Constants.NOTIFICATION_ID_SERVICE, notification)
    }

    private fun shouldNotifyForBlock(activityType: ActivityType): Boolean {
        return activityType in listOf(
            ActivityType.SITTING,
            ActivityType.WALKING,
            ActivityType.CHANTING,
            ActivityType.DHAMMA_TALK
        )
    }

    private suspend fun updateWidget(context: Context) {
        com.soloretreat.ui.widget.RetreatWidget().updateAll(context)
    }
}
