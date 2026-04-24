package com.soloretreat.util

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object TimeUtils {
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    private val fullDateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, HH:mm")

    fun formatTime(time: LocalTime): String = time.format(timeFormatter)

    fun formatFullDate(date: LocalDate): String = date.format(fullDateFormatter)

    fun formatDateTime(instant: Instant): String {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            .format(dateTimeFormatter)
    }

    fun formatDuration(minutes: Long): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return when {
            hours > 0 && mins > 0 -> "${hours}h ${mins}m"
            hours > 0 -> "${hours}h"
            else -> "${mins}m"
        }
    }

    fun localDateTimeToInstant(date: LocalDate, time: LocalTime): Instant {
        return LocalDateTime.of(date, time)
            .atZone(ZoneId.systemDefault())
            .toInstant()
    }

    fun daysBetween(start: LocalDate, end: LocalDate): Int {
        return Duration.between(start.atStartOfDay(), end.plusDays(1).atStartOfDay()).toDays().toInt()
    }

    fun dayOfRetreat(startDate: LocalDate): Int {
        val today = LocalDate.now()
        val diff = Duration.between(startDate.atStartOfDay(), today.atStartOfDay()).toDays().toInt()
        return if (diff < 0) 1 else diff + 1
    }

    fun isMealCutoffApproaching(): Boolean {
        val now = LocalTime.now()
        val cutoff = LocalTime.of(12, 0)
        return now.isBefore(cutoff) && Duration.between(now, cutoff).toMinutes() <= 30
    }
}