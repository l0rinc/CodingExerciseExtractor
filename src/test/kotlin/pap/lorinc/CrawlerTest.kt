package pap.lorinc

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import pap.lorinc.LeetCode.LeetCodeCrawler.parseDuration
import java.time.LocalDateTime
import kotlin.test.expect

class ParseDurationTests : Spek({
    data class Data(val input: String, val expected: LocalDateTime)

    describe("parseDuration") {
        val now = LocalDateTime.now()
        listOf(
                Data("0 minutes ago", now.minusMinutes(0)),
                Data("1 minute ago", now.minusMinutes(1)),
                Data("2 minutes ago", now.minusMinutes(2)),
                Data("11 hours, 39 minutes ago", now.minusHours(11).minusMinutes(39)),
                Data("1 day ago", now.minusDays(1)),
                Data("2 days, 12 hours ago", now.minusDays(2).minusHours(12)),
                Data("1 week ago", now.minusWeeks(1)),
                Data("1 week, 1 day ago", now.minusWeeks(1).minusDays(1)),
                Data("3 weeks, 5 days ago", now.minusWeeks(1).minusDays(3)),
                Data("1 month ago", now.minusMonths(1)),
                Data("2 months ago", now.minusMonths(2)),
                Data("2 months, 1 week ago", now.minusMonths(2).minusWeeks(1)),
                Data("5 month, 3 weeks ago", now.minusMonths(5).minusWeeks(3)),
                Data("1 year ago", now.minusYears(1)),
                Data("2 years ago", now.minusYears(2)),
                Data("2 year, 1 week ago", now.minusYears(2).minusWeeks(1)),
                Data("2 years, 2 weeks ago", now.minusYears(2).minusWeeks(2))

        ).forEach {
            expect(it.expected) { parseDuration(it.input, now) }
        }
    }
})
