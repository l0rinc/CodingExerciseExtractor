package pap.lorinc.CodingBat

import pap.lorinc.Utils

fun generateCommands(contents: List<CodingBatProblem>) = {
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
        >${Utils.echo(generateMain(info))} > $mainJava/${info.className}.java
        >${Utils.echo(generateTest(info, testClassName))}> $testSpock/$testClassName.groovy
        >git add src
        >git commit -m "${info.packageName} / ${info.className}" --date="${info.date}"
        >
        """.trimMargin(">")
    }.joinToString("\n")

    val after = """
        >
        >
        >${Utils.echo("to install gradle, type: sudo add-apt-repository ppa:cwchien/gradle && sudo apt-get update && sudo apt-get install gradle")}
        >${Utils.echo("[![Build Status](https://travis-ci.org/$userName/CodingBatSolutions.png)](https://travis-ci.org/$userName/CodingBatSolutions)\n\nSolutions to my [CodingBat](http://codingbat.com/java) exercises, exported by [CodingBat2GitHub](https://github.com/paplorinc/CodingBat2GitHub).")} > README.md
        >${Utils.echo("language: groovy\n\njdk: oraclejdk8\n\nbefore_install: chmod +x gradlew\nscript: ./gradlew clean build --stacktrace")} > .travis.yml
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
        > * ${info.description.prependIndent(" * ")}
        >
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