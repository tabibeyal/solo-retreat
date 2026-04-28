package com.soloretreat.util

import com.soloretreat.data.local.entity.ScheduleBlock
import com.soloretreat.data.model.ActivityType
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll
import java.time.LocalTime

class ScheduleValidatorPropertyTest : StringSpec({
    val arbLocalTime = arbitrary {
        val hour = Arb.int(0..23).bind()
        val minute = Arb.int(0..59).bind()
        LocalTime.of(hour, minute)
    }

    val arbScheduleBlock = arbitrary {
        val day = Arb.int(0..10).bind()
        val start = arbLocalTime.bind()
        // Ensure end is after start for a "valid" block structure
        val durationMinutes = Arb.int(1..120).bind()
        val end = start.plusMinutes(durationMinutes.toLong())
        val type = Arb.enum<ActivityType>().bind()
        ScheduleBlock(
            dayOffset = day,
            startTime = start,
            endTime = end,
            activityType = type
        )
    }

    "Empty list of blocks should return NoBlocks" {
        ScheduleValidator.validate(emptyList()) shouldBe ScheduleValidator.ValidationResult.NoBlocks
    }

    "Valid non-overlapping blocks before midday should be Valid" {
        // This is tricky to generate purely randomly and always be valid, 
        // but we can test that if we have a single block before 12, it's valid.
        checkAll(arbScheduleBlock) { block ->
            val testBlock = if (block.activityType == ActivityType.MEAL) {
                block.copy(startTime = LocalTime.of(8, 0), endTime = LocalTime.of(9, 0))
            } else {
                block
            }
            
            // If it's a meal and it happens to be after 12, we skip or adjust
            val finalBlock = if (testBlock.activityType == ActivityType.MEAL && testBlock.endTime.isAfter(LocalTime.of(12, 0))) {
                 testBlock.copy(startTime = LocalTime.of(10, 0), endTime = LocalTime.of(11, 0))
            } else {
                testBlock
            }

            ScheduleValidator.validate(listOf(finalBlock)) shouldBe ScheduleValidator.ValidationResult.Valid
        }
    }

    "Meal after 12:00 should be MealTooLate" {
        val arbLateMeal = arbitrary {
            val start = LocalTime.of(12, 1)
            val end = LocalTime.of(13, 0)
            ScheduleBlock(
                dayOffset = 0,
                startTime = start,
                endTime = end,
                activityType = ActivityType.MEAL
            )
        }
        checkAll(arbLateMeal) { meal ->
            ScheduleValidator.validate(listOf(meal)).shouldBeInstanceOf<ScheduleValidator.ValidationResult.MealTooLate>()
        }
    }
})
