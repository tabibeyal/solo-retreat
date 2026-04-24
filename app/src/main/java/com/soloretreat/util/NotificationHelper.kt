package com.soloretreat.util

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.soloretreat.R
import com.soloretreat.data.model.ActivityType
import com.soloretreat.service.MeditationTimerService
import com.soloretreat.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private fun createBaseNotification(channelId: String): NotificationCompat.Builder {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    }

    fun buildOngoingRetreatNotification(day: Int, totalDays: Int, nextBlock: String?, nextTime: String?): Notification {
        val content = if (nextBlock != null && nextTime != null) {
            "Next: $nextBlock at $nextTime"
        } else {
            "No more blocks today"
        }

        val stopIntent = Intent(context, com.soloretreat.service.RetreatStopReceiver::class.java).apply {
            action = Constants.ACTION_STOP_RETREAT
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context, Constants.ALARM_REQUEST_STOP, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return createBaseNotification(Constants.CHANNEL_RETREAT_SERVICE)
            .setContentTitle("Retreat in Progress \u2014 Day $day of $totalDays")
            .setContentText(content)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(R.drawable.ic_stop, "End Retreat", stopPendingIntent)
            .build()
    }

    fun buildBlockReminderNotification(activityType: ActivityType, blockName: String): Notification {
        val title = when (activityType) {
            ActivityType.SITTING -> "Sitting Meditation Soon"
            ActivityType.WALKING -> "Walking Meditation Soon"
            ActivityType.CHANTING -> "Chanting Session Soon"
            ActivityType.DHAMMA_TALK -> "Dhamma Talk Soon"
            ActivityType.MEAL -> "Meal Time Soon"
            ActivityType.REST -> "Rest Period Soon"
            ActivityType.SLEEP -> "Sleep Period Soon"
        }
        return createBaseNotification(Constants.CHANNEL_BLOCK_REMINDER)
            .setContentTitle(title)
            .setContentText("Your $blockName starts in 5 minutes")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    fun buildMealCutoffNotification(): Notification {
        return createBaseNotification(Constants.CHANNEL_MEAL_CUTOFF)
            .setContentTitle("Meal Cutoff Approaching")
            .setContentText("No solid food after 12:00 PM. Please complete your meal.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    fun buildTimerNotification(
        activityLabel: String,
        remainingSeconds: Long,
        isPaused: Boolean
    ): Notification {
        val stopIntent = Intent(context, MeditationTimerService::class.java).apply {
            action = Constants.ACTION_STOP_TIMER
        }
        val stopPending = PendingIntent.getService(
            context, Constants.ALARM_REQUEST_TIMER_STOP, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val status = if (isPaused) "Paused" else "In Progress"
        return createBaseNotification(Constants.CHANNEL_TIMER_SERVICE)
            .setContentTitle(activityLabel)
            .setContentText("${TimerFormatter.formatSeconds(remainingSeconds)} — $status")
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(R.drawable.ic_stop, "Stop", stopPending)
            .build()
    }

    fun notifyTimer(notification: Notification) {
        try {
            NotificationManagerCompat.from(context)
                .notify(Constants.NOTIFICATION_ID_TIMER, notification)
        } catch (_: SecurityException) {
        }
    }

    fun buildPersistentMealReminder(): Notification {
        return createBaseNotification(Constants.CHANNEL_MEAL_CUTOFF)
            .setContentTitle("Meal Not Logged")
            .setContentText("It is past noon. Remember to log your meal status.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

}