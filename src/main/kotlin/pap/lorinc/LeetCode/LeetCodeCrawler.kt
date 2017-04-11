package pap.lorinc.LeetCode

import com.beust.klaxon.*
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.Duration
import java.time.LocalDateTime

enum class Language { CPP, JAVA, PYTHON, C, CSHARP, JAVASCRIPT, RUBY, SWIFT, GOLANG }
data class LeetCodeProblem(val submitTime: LocalDateTime, val packageName: String, val link: String, val description: String, val solution: String, val name: String, val runTime: Duration, val language: Language)

object Crawler2 {
    val base = "https://leetcode.com"

    @JvmStatic fun main(args: Array<String>) {
        val cookies = login().cookies()
        val contents = parseContent(cookies)
        val commands = generateCommands(contents)
        println(commands())
    }

    private fun parseContent(cookies: MutableMap<String, String>): List<LeetCodeProblem> {
        val visited = hashSetOf<String>()
        val results = mutableListOf<LeetCodeProblem>()
        for (page in 1..maxPageCount) {
            val parsedSubmissions = submissions(page, cookies)

            val solutions = solutions(cookies, parsedSubmissions, visited)
            results.addAll(solutions)

            if (parsedSubmissions.boolean("has_next") != true)
                break

            println("Page $page complete with: ${solutions.map { "'${it.name}'" }.joinToString(",")}")
        }
        return results.reversed()
    }

    private fun solutions(cookies: MutableMap<String, String>, parsedSubmissions: JsonObject, visited: HashSet<String>): List<LeetCodeProblem> =
            parsedSubmissions.array<JsonObject>("submissions_dump")!!
                    .filter { s -> s.string("status_display") == "Accepted" }
                    .filter { s -> visited.add(s.string("title")!!) }
                    .map { submission ->
                        val solution = Jsoup.connect(base + submission.string("url")).cookies(cookies).get()
                        LeetCodeProblem(
                                submitTime = parseDuration(submission),
                                packageName = parsePackage(submission),
                                link = parseLink(solution),
                                description = getDescription(solution),
                                solution = getSolution(solution),
                                name = getName(submission),
                                runTime = getRunTime(submission),
                                language = parseLanguage(submission)
                        )
                    }

    private fun submissions(page: Int, cookies: MutableMap<String, String>): JsonObject {
        val submissions = Jsoup
                .connect("$base/api/submissions/my/$page/?format=json")
                .cookies(cookies)
                .ignoreContentType(true)
                .get().body().text()
        return Parser().parse(StringBuilder(submissions)) as JsonObject
    }

    private fun login(): Connection.Response {
        val loginForm = Jsoup
                .connect("$base/accounts/login/")
                .execute()

        val token = loginForm.parse().select("""input[name="csrfmiddlewaretoken"]""").attr("value")
        return Jsoup
                .connect("$base/accounts/login/")
                .method(Connection.Method.POST)
                .referrer("$base/accounts/login/")
                .data("csrfmiddlewaretoken", token)
                .data("login", userId)
                .data("password", password)
                .cookies(loginForm.cookies())
                .execute()
    }

    private fun parseDuration(submission: JsonObject): LocalDateTime {
        val submissionTime = submission.string("time")!!.replace(Regex("""\W+"""), " ")
        return Regex("""^(?:(\d+) years?)?(?: *(\d+) months?)?(?: *(\d+) weeks?)?(?: *(\d+) +days?)?(?: *(\d+) hours?)?(?: *(\d+) minutes?)?$""")
                .matchEntire(submissionTime)!!.destructured.let { (year, month, week, day, hour, minute) ->
            LocalDateTime.now()
                    .minusYears(year.toLongOrNull() ?: 0)
                    .minusMonths(month.toLongOrNull() ?: 0)
                    .minusWeeks(week.toLongOrNull() ?: 0)
                    .minusDays(day.toLongOrNull() ?: 0)
                    .minusHours(hour.toLongOrNull() ?: 0)
                    .minusMinutes(minute.toLongOrNull() ?: 0)
        }
    }

    private fun parsePackage(submission: JsonObject): String = submission.string("title")!!.trim().replace(Regex("(?i)[^a-z]"), "").decapitalize()
    private fun getDescription(solution: Document): String = solution.select("""meta[name="description"]""").attr("content").replace(Regex("[\r\n]{2,}"), "\n\n")
    private fun parseLink(solution: Document): String = base + solution.select("""a[href^="/problems/"]""").first().attr("href")
    private fun getSolution(solution: Document): String {
        val code = Regex("submissionCode: '(.+)',").find(solution.select("script")[7].html())!!.groupValues[1]
        return code.replace(Regex("""\\u(....)""")) { m -> m.groupValues[1].toLong(16).toChar().toString() }
    }
    private fun getName(submission: JsonObject): String = submission.string("title")!!
    private fun getRunTime(submission: JsonObject): Duration = Duration.ofMillis(submission.string("runtime")!!.replace(Regex("""\D+"""), "").toLong())
    private fun parseLanguage(submission: JsonObject): Language = Language.valueOf(submission.string("lang")!!.toUpperCase())
}