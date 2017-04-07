package pap.lorinc

import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.lang.Long.parseLong
import java.time.Duration
import java.time.LocalDateTime

enum class Language { CPP, JAVA, PYTHON, C, CSHARP, JAVASCRIPT, RUBY, SWIFT, GOLANG }
data class LeetCodeProblem(val submitTime: LocalDateTime, val url: String, val name: String, val runTime: Duration, val language: Language)

object Crawler2 {
    val base = "https://leetcode.com"

    var userId   = "" // your userId here
    var password = "" // your password here

    @JvmStatic fun main(args: Array<String>) {
        val loginForm = Jsoup
                .connect("$base/accounts/login/")
                .execute()

        System.out.println(loginForm.cookies())

        val token = loginForm.parse().select("input[name=\"csrfmiddlewaretoken\"]").attr("value")
        val document = Jsoup
                .connect("$base/accounts/login/")
                .method(Connection.Method.POST)
                .referrer("$base/accounts/login/")
                .data("csrfmiddlewaretoken", token)
                .data("login", userId)
                .data("password", password)
                .cookies(loginForm.cookies())
                .execute()

        System.out.println(document)

        val submissions = Jsoup
                .connect("$base/submissions/")
                .method(Connection.Method.GET)
                .cookies(document.cookies())
                .execute()
                .parse()

        val lines = submissions.select("tbody > tr")
                .filter { r -> r.child(2).text().contains("Accepted") }
                .map { prob ->
                    val (submitTime, question, _, runTime, language) = prob.children()
                    LeetCodeProblem(
                            submitTime = parseDuration(submitTime),
                            url = getUrl(question),
                            name = getName(question),
                            runTime = getRunTime(runTime),
                            language = parseLanguage(language)
                    )
                }

        System.out.println(lines)
    }

    private fun parseDuration(trim: Element): LocalDateTime =
            Regex("^(?:(\\d+) years?)?(?: *(\\d+) months?)?(?: *(\\d+) weeks?)?(?: *(\\d+) +days?)?(?: *(\\d+) hours?)?(?: *(\\d+) minutes?)?\\s+ago$")
                    .matchEntire(trim.text().replace(Regex("\\W+"), " ")).let {
                val (year, month, week, day, hour, minute) = it!!.destructured
                LocalDateTime.now()
                        .plusYears(year.toLongOrNull() ?: 0)
                        .plusMonths(month.toLongOrNull() ?: 0)
                        .plusWeeks(week.toLongOrNull() ?: 0)
                        .plusDays(day.toLongOrNull() ?: 0)
                        .plusHours(hour.toLongOrNull() ?: 0)
                        .plusMinutes(minute.toLongOrNull() ?: 0)
            }

    private fun getUrl(question: Element): String = base + question.select("a").attr("href")
    private fun getName(question: Element): String = question.select("a").text()
    private fun getRunTime(runTime: Element): Duration = Duration.ofMillis(parseLong(runTime.text().replace(Regex("\\D+"), "")))
    private fun parseLanguage(s: Element) = Language.valueOf(s.text().toUpperCase())
}