import java.util.stream.Collectors

fun getKernelVersion(): Version =
    Runtime.getRuntime().exec(arrayOf("uname", "-r"))
        .also { it.waitFor() }
        .inputReader()
        .lines()
        .collect(Collectors.joining())
        .let { version -> version.substring(0, version.indexOf("-")) }
        .let { version -> Version(version) }

fun main() {
    println(getKernelVersion())
}
