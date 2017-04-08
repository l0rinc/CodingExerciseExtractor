## Export your [CodingBat](http://codingbat.com/java) & [LeetCode](https://leetcode.com/problemset/algorithms) solutions to GitHub

### Descripion
This crawler will gather all your solved exercises, their descriptions, submit times, difficulty and even tests, if possible.

It will generate a series of `git` commits (as command line script) for each exercise, with the correct date, message and content.

It will also add a basic `gradle` build and run it after the script is run.

### Example:
See my own exported solutions at: [CodingBatSolutions](https://github.com/paplorinc/CodingBatSolutions) 

### Steps:
* Import the provided `Kotlin` & `Jsoup` script in `Idea`;
* Add your `username` and `password` to the script (optionally followed by the number of exercises to skip);
  * or add them as arguments, in the same order;
* Run the main and wait for the output;
  * `git`, `gradle` and `bash` command will be generated (nothing is run), you can decide what to execute - linux only;
* Paste and run the content of the generated commands in the terminal, in the empty folder where you want the files to be placed;
* Sit back and relax while `git` is initialized, the solutions and tests are committed and a `Gradle` project structure is generated.