package com.soloretreat.util

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll

class TimeUtilsPropertyTest : StringSpec({
    "formatDuration should always contain 'h' for durations >= 60 minutes" {
        checkAll(Arb.long(60L..10000L)) { minutes ->
            TimeUtils.formatDuration(minutes) shouldContain "h"
        }
    }

    "formatDuration should always contain 'm' for durations not divisible by 60" {
        checkAll(Arb.long(1L..10000L)) { minutes ->
            if (minutes % 60 != 0L) {
                TimeUtils.formatDuration(minutes) shouldContain "m"
            }
        }
    }

    "formatDuration should return only 'm' for durations < 60 minutes" {
        checkAll(Arb.long(1L..59L)) { minutes ->
            val result = TimeUtils.formatDuration(minutes)
            result shouldContain "m"
            result shouldNotContain "h"
        }
    }
})
