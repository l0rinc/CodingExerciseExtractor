package pap.lorinc.CodingBat

import org.jsoup.Connection
import org.jsoup.Jsoup
import pap.lorinc.Utils.echo

data class Test(val methodCall: String, val expected: String)
data class CodingBatProblem(val packageName: String, val className: String, val methodName: String, val link: String, val date: String, val description: String, val content: String, val tests: List<Test>)

object Crawler {
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

    private fun generateCommands(contents: List<CodingBatProblem>) = {
        val userName = userId.replace(Regex("@.+$"), "")

        val before = """
            >
            >git init
            >
            """.trimMargin(">")

        val git = contents.map { info ->
            val testClassName = "${info.className}Test"
            val (mainJava, testSpock) = arrayOf("main/java", "test/groovy").map { "src/$it/${info.packageName}" }
            """
            >
            >mkdir -p '$mainJava' '$testSpock'
            >${echo(generateMain(info))} > $mainJava/${info.className}.java
            >${echo(generateTest(info, testClassName))}> $testSpock/$testClassName.groovy
            >git add src
            >git commit -m "${info.packageName} / ${info.className}" --date="${info.date}"
            >
            """.trimMargin(">")
        }.joinToString("\n")

        val after = """
            >
            >
            >${echo("to install gradle, type: sudo add-apt-repository ppa:cwchien/gradle && sudo apt-get update && sudo apt-get install gradle")}
            >${echo("[![Build Status](https://travis-ci.org/$userName/CodingBatSolutions.png)](https://travis-ci.org/$userName/CodingBatSolutions)\n\nSolutions to my [CodingBat](http://codingbat.com/java) exercises, exported by [CodingBat2GitHub](https://github.com/paplorinc/CodingBat2GitHub).")} > README.md
            >${echo("language: groovy\n\njdk: oraclejdk8\n\nbefore_install: chmod +x gradlew\nscript: ./gradlew clean build --stacktrace")} > .travis.yml
            >gradle init --type java-library --test-framework spock && rm src/test/groovy/LibraryTest.groovy && rm src/main/java/Library.java && git add -A && gradle build
            >
            """.trimMargin(">")

        if (skip > 0) git + "gradle build"
        else before + git + after
    }

    private fun generateMain(info: CodingBatProblem) = """
        >package ${info.packageName};
        >
        >import java.util.*;
        >
        >/**
        > * ${info.description.lines().map(String::trim).joinToString("\n * ")}
        > * Source: ${info.link}
        > */
        >public class ${info.className} {
        >  ${info.content.lines().joinToString("\n  ").trim()}
        >}
        >""".trimMargin(">").trim()

    private fun generateTest(info: CodingBatProblem, testClassName: String) = """
        >package ${info.packageName};
        >
        >import spock.lang.Specification
        >
        >class $testClassName extends Specification {
        >  def '${info.methodName}'() {
        >    setup:
        >      def subject = new ${info.className}()
        >    expect:
        >${info.tests.map { test -> """
        >      subject.${fix(info, test)} == ${fix(test)}""".trimMargin(">") }.joinToString("\n")}
        >  }
        >}
        >""".trimMargin(">").trim()

    private fun fix(test: Test): String = test.expected.replace("Th a FH", "Th  a FH")
    private fun fix(info: CodingBatProblem, test: Test): String {
        var result = test.methodCall.replace('"', '\'').replace('{', '[').replace('}', ']')
        val declaration = info.content.lines().first()
        if (declaration.contains("int[]")) result = result.replace("]", "] as int[]")
        else if (declaration.contains("String[]")) result = result.replace("]", "] as String[]")
        return result
    }
}