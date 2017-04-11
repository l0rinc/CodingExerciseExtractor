package pap.lorinc

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import pap.lorinc.LeetCode.LeetCodeCrawler.parseDuration
import java.time.LocalDateTime
import kotlin.test.expect

@RunWith(JUnitPlatform::class)
class ParseDurationTests : Spek({
    data class Data(val input: String, val expected: LocalDateTime)

    describe("parseDuration") {
        val now = LocalDateTime.now()
        listOf(
                Data("0 minutes", now.minusMinutes(0)),
                Data("1 minute", now.minusMinutes(1)),
                Data("2 minutes", now.minusMinutes(2)),
                Data("11 hours, 39 minutes", now.minusHours(11).minusMinutes(39)),
                Data("1 day", now.minusDays(1)),
                Data("2 days, 12 hours", now.minusDays(2).minusHours(12)),
                Data("1 week", now.minusWeeks(1)),
                Data("1 week, 1 day", now.minusWeeks(1).minusDays(1)),
                Data("3 weeks, 5 days", now.minusWeeks(3).minusDays(5)),
                Data("1 month", now.minusMonths(1)),
                Data("2 months", now.minusMonths(2)),
                Data("2 months, 1 week", now.minusMonths(2).minusWeeks(1)),
                Data("5 month, 3 weeks", now.minusMonths(5).minusWeeks(3)),
                Data("1 year", now.minusYears(1)),
                Data("2 years", now.minusYears(2)),
                Data("2 year, 1 week", now.minusYears(2).minusWeeks(1)),
                Data("2 years, 2 weeks", now.minusYears(2).minusWeeks(2))

        ).forEach {
            expect(it.expected, it.input) { parseDuration(it.input, now) }
        }
    }
})
