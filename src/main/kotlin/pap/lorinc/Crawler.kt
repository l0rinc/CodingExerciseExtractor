package pap.lorinc

import org.jsoup.Connection
import org.jsoup.Jsoup

data class Test(val methodCall: String, val expected: String)
data class Problem(val packageName: String, val className: String, val methodName: String, val link: String, val date: String, val description: String, val content: String, val tests: List<Test>)

object Crawler {
    val base = "http://codingbat.com"

    var userId   = "" // your userId here
    var password = "" // your password here
    var skip     = 0  // your finished and committed exercise count

    @JvmStatic fun main(args: Array<String>) {
        init(args)
        val contents = parseContent()
        val commands = generateCommands(contents())
        println(commands())
    }

    private fun init(args: Array<String>) {
        userId = args.getOrElse(0) { i -> userId }
        password = args.getOrElse(1) { i -> password }
        skip = args.getOrElse(2) { i -> skip.toString() }.toInt()
        assert(userId.isNotEmpty() && password.isNotEmpty() && skip >= 0) { "You need to provide some authentication info!" }
    }

    private fun parseContent() = {
        val cookies = login()
        Jsoup.connect("$base/done").cookies(cookies).get()
             .select("a[href^=/prob/]").drop(skip)
             .map { prob ->
                 val probLink = base + prob.attr("href")
                 val probDoc = Jsoup.connect(probLink).cookies(cookies).get()

                 val packageName = probDoc.select("body > div.tabc > div > div > a:nth-child(1) > span").first().text().replace(Regex("\\W+"), "_").toLowerCase()
                 val methodName = probDoc.select("body > div.tabc > div > div > span").first().text()
                 val className = methodName.capitalize()
                 val date = prob.nextSibling().nextSibling().childNode(0).toString()
                 val description = probDoc.select("div.minh").first().text().trim()
                 val content = probDoc.select("#ace_div").first().textNodes().first().wholeText.trim()

                 val id = probDoc.select("input[name=id]").first().attr("value")
                 val tests = getTests(content, cookies, id)

                 Problem(packageName, className, methodName, probLink, date, description, content, tests);
             }
    }

    private fun login(): Map<String, String> {
        return Jsoup.connect("$base/login")
                    .method(Connection.Method.POST)
                    .data("uname", userId)
                    .data("pw", password)
                    .data("dologin", "log in")
                    .data("fromurl", "/java")
                    .execute()
                    .cookies()
    }

    private fun getTests(content: String, cookies: Map<String, String>, id: String): List<Test> {
        val testsDoc = Jsoup.connect("$base/run")
                            .cookies(cookies)
                            .data("id", id)
                            .data("code", content)
                            .data("cuname", userId)
                            .post()
        val delimiter = " → "
        val tests = testsDoc.select("#tests > table > tbody > tr > td:nth-child(1)").map { it.text() }.filter { it.contains(delimiter) }.map {
            val (methodCall, expected) = it.split(delimiter)
            Test(methodCall, expected)
        }
        return tests
    }

    private fun generateCommands(contents: List<Problem>) = {
        val before = """
            |
            |git init
            |
            """.trimMargin()

        val git = contents.map { info ->
            val testClassName = "${info.className}Test"
            val (mainJava, testSpock) = arrayOf("main/java", "test/groovy").map { "src/${it}/${info.packageName}" }
            """
            |
            |mkdir -p '${mainJava}' '${testSpock}'
            |${echo(generateMain(info))} > ${mainJava}/${info.className}.java
            |${echo(generateTest(info, testClassName))}> ${testSpock}/${testClassName}.groovy
            |git add src
            |git commit -m "${info.packageName} / ${info.className}" --date="${info.date}"
            |
            """.trimMargin()
        }.joinToString("\n")

        val userName = userId.replace(Regex("@.+$"), "")
        val after = """
            |
            |
            |${echo("to install gradle, type: sudo add-apt-repository ppa:cwchien/gradle && sudo apt-get update && sudo apt-get install gradle")}
            |${echo("[![Build Status](https://travis-ci.org/${userName}/CodingBatSolutions.png)](https://travis-ci.org/${userName}/CodingBatSolutions)\n\nSolutions to my [CodingBat](http://codingbat.com/java) exercises, exported by [CodingBat2GitHub](https://github.com/paplorinc/CodingBat2GitHub).")} > README.md
            |${echo("language: groovy\n\njdk: oraclejdk8\n\nbefore_install: chmod +x gradlew\nscript: ./ gradlew clean build --stacktrace")} > .travis.yml
            |gradle init --type java-library --test-framework spock && rm src/test/groovy/LibraryTest.groovy && rm src/main/java/Library.java && git add -A && gradle build
            |
            """.trimMargin()
        if (skip > 0) git
        else before + git + after
    }

    private fun generateMain(info: Problem) =
        """
        |package ${info.packageName};
        |
        |import java.util.*;
        |
        |/**
        | * ${info.description.lines().map { it.trim() }.joinToString("\n * ")}
        | * Source: ${info.link}
        | */
        |public class ${info.className} {
        |  ${info.content.lines().joinToString("\n  ").trim()}
        |}
        |""".trimMargin()

    private fun generateTest(info: Problem, testClassName: String) =
        """
        |package ${info.packageName};
        |
        |import spock.lang.Specification
        |
        |class ${testClassName} extends Specification {
        |  def '${info.methodName}'() {
        |    setup:
        |      def subject = new ${info.className}()
        |    expect:
        |${info.tests.map { test -> """
        |      subject.${fix(info, test)} == ${fix(test)}""".trimMargin() }.joinToString("\n")}
        |  }
        |}
        |""".trimMargin()

    private fun fix(test: Test) = test.expected.replace("Th a FH", "Th  a FH")
    private fun fix(info: Problem, test: Test): String {
        var result = test.methodCall.replace('"', '\'')
        val declaration = info.content.lines().first()
        if (declaration.contains("int[]")) result = result.replace("]", "] as int[]")
        else if (declaration.contains("String[]")) result = result.replace("]", "] as String[]")
        return result
    }

    private fun echo(text: String) = """echo -e $'${text.replace("\\", "\\\\\\\\").replace("'", "\\'").replace("\n", "\\n")}'"""
}