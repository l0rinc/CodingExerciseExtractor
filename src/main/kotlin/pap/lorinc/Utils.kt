package pap.lorinc

object Utils {
    fun echo(text: String): String {
        val fixEscapes = text.replace("""\""", """\\\\""").replace("'", """\'""")
        val fixLines = fixEscapes.replace("\t", "    ").replace("\n", """\n""").replace(" +\n", "\n")
        return "echo -e $'$fixLines'"
    }
}