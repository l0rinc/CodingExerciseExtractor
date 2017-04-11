package pap.lorinc.CodingBat

import org.jsoup.Connection
import org.jsoup.Jsoup

data class Test(val methodCall: String, val expected: String)
data class CodingBatProblem(val packageName: String, val className: String, val methodName: String, val link: String, val date: String, val description: String, val content: String, val tests: List<Test>)

object CodingBatCrawler {
    val base = "http://codingbat.com"

    @JvmStatic fun main(args: Array<String>) {
        val cookies = login().cookies()
        val contents = parseContent(cookies)
        val commands = generateCommands(contents)
        println(commands())
    }

    private fun parseContent(cookies: Map<String, String>): List<CodingBatProblem> =
            Jsoup.connect("$base/done").cookies(cookies).get()
                    .select("a[href^=/prob/]").drop(skip)
                    .map { prob ->
                        val probLink = base + prob.attr("href")
                        val probDoc = Jsoup.connect(probLink).cookies(cookies).get()

                        val packageName = probDoc.select("body > div.tabc > div > div > a:nth-child(1) > span").first().text().replace(Regex("""\W+"""), "_").toLowerCase()
                        val methodName = probDoc.select("body > div.tabc > div > div > span").first().text()
                        val className = methodName.capitalize()
                        val date = prob.nextSibling().nextSibling().childNode(0).toString()
                        val description = probDoc.select("div.minh").first().text().trim()
                        val content = probDoc.select("#ace_div").first().textNodes().first().wholeText.trim()

                        val id = probDoc.select("input[name=id]").first().attr("value")
                        val tests = getTests(content, id)

                        CodingBatProblem(packageName, className, methodName, probLink, date, description, content, tests)
                    }

    private fun login(): Connection.Response = Jsoup
            .connect("$base/login")
            .method(Connection.Method.POST)
            .data("uname", userId)
            .data("pw", password)
            .data("dologin", "log in")
            .data("fromurl", "/java")
            .execute()

    private fun getTests(content: String, id: String): List<Test> {
        val testsDoc = Jsoup
                .connect("$base/run")
                .data("id", id)
                .data("code", content)
                .post()
        val delimiter = " → "
        val tests = testsDoc.select("#tests > table > tbody > tr > td:nth-child(1)").map { it.text() }.filter { it.contains(delimiter) }.map {
            val (methodCall, expected) = it.split(delimiter)
            Test(methodCall, expected)
        }
        return tests
    }
}