package com.soloretreat.util

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll

class TimerFormatterPropertyTest : StringSpec({
    "formatSeconds should always be in MM:SS format or longer" {
        checkAll(Arb.long(0L..100000L)) { totalSeconds ->
            val result = TimerFormatter.formatSeconds(totalSeconds)
            result shouldMatch Regex("""\d{2,}:\d{2}""")
        }
    }

    "formatSeconds for less than 10 seconds should end with :0X" {
        checkAll(Arb.long(0L..9L)) { seconds ->
            val result = TimerFormatter.formatSeconds(seconds)
            result shouldBe "00:0$seconds"
        }
    }
})
