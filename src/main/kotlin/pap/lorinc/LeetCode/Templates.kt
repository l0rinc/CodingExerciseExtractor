package pap.lorinc.LeetCode

import pap.lorinc.Utils

fun generateCommands(contents: List<LeetCodeProblem>) = {
    val userName = userId.replace(Regex("@.+$"), "")
    val languages = contents.map { it.language.toString().toLowerCase() }.toSet().joinToString(",")
    if ("java" != languages) throw IllegalStateException("Only Java is supported for now!")
    val sourcesFolder = "src/main/${languages}/leetcode/"

    val before = """
        >
        >git init
        >
        """.trimMargin(">")

    val git = contents.map { info ->
        val mainJava = sourcesFolder + info.packageName
        val className = className(info)
        """
        >
        >mkdir -p '$mainJava'
        >${Utils.echo(generateMain(info))} > $mainJava/$className.java
        >git add src
        >git commit -m "${info.name}" --date="${info.submitTime}"
        >
        """.trimMargin(">")
    }.joinToString("\n")

    val after = """
        >
        >
        >${library.map { "\n>echo('${it.value}') > $sourcesFolder/${it.key}.java" }.joinToString()}
        >
        >${Utils.echo("to install gradle, type: sudo add-apt-repository ppa:cwchien/gradle && sudo apt-get update && sudo apt-get install gradle")}
        >${Utils.echo("[![Build Status](https://travis-ci.org/$userName/LeetCodeSolutions.png)](https://travis-ci.org/$userName/LeetCodeSolutions)\n\nSolutions to my [LeetCode](http://LeetCode.com) exercises, exported by [CodingExerciseExtractor](https://github.com/paplorinc/CodingExerciseExtractor).")} > README.md
        >${Utils.echo("language: $languages\n\njdk: oraclejdk8\n\nbefore_install: chmod +x gradlew\nscript: ./gradlew clean build --stacktrace")} > .travis.yml
        >gradle init --type java-library --test-framework spock && rm src/test/groovy/LibraryTest.groovy && rm src/main/java/Library.java && git add -A && gradle build
        >
        """.trimMargin(">")

    before + git + after
}

fun className(info: LeetCodeProblem) =
        Regex("""^public class (\w+)""", RegexOption.MULTILINE)
                .find(info.solution)
                ?.groupValues
                ?.get(1)
                ?: "Solution"

fun generateMain(info: LeetCodeProblem) = """
        >package leetcode.${info.packageName};
        >
        >import java.util.*;
        >import java.util.stream.*;
        >import java.util.function.*;
        >import leetcode.*;
        >
        >/**
        > * ${info.description.prependIndent(" * ")}
        > *
        > * Source: ${info.link}
        > */
        >${info.solution}
        >""".trimMargin(">").trim()
