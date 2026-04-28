package com.soloretreat.util

import com.soloretreat.data.local.entity.ScheduleBlock
import com.soloretreat.data.model.ActivityType
import java.time.LocalTime

object ScheduleValidator {
    sealed class ValidationResult {
        data object Valid : ValidationResult()
        data object NoBlocks : ValidationResult()
        data class Overlap(val dayOffset: Int, val block1: String, val block2: String) : ValidationResult()
        data class MealTooLate(val dayOffset: Int, val endTime: LocalTime) : ValidationResult()
    }

    fun validate(blocks: List<ScheduleBlock>): ValidationResult {
        if (blocks.isEmpty()) return ValidationResult.NoBlocks

        val grouped = blocks.groupBy { it.dayOffset }

        for ((dayOffset, dayBlocks) in grouped) {
            val sorted = dayBlocks.sortedBy { it.startTime }

            for (i in 0 until sorted.size - 1) {
                val current = sorted[i]
                val next = sorted[i + 1]
                if (next.startTime.isBefore(current.endTime)) {
                    return ValidationResult.Overlap(
                        dayOffset = dayOffset,
                        block1 = "${current.activityType.displayName} (${current.startTime}-${current.endTime})",
                        block2 = "${next.activityType.displayName} (${next.startTime}-${next.endTime})"
                    )
                }
            }

            val mealBlocks = sorted.filter { it.activityType == ActivityType.MEAL }
            for (meal in mealBlocks) {
                if (meal.endTime.isAfter(LocalTime.of(12, 0))) {
                    return ValidationResult.MealTooLate(
                        dayOffset = meal.dayOffset,
                        endTime = meal.endTime
                    )
                }
            }
        }

        return ValidationResult.Valid
    }
}
