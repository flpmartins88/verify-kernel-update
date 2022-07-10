import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import kotlin.system.exitProcess

const val baseUrl = "https://kernel.ubuntu.com/~kernel-ppa/mainline/"
const val archtecture = "amd64"

data class VersionInfo(
    val version: Version,
    val releaseDate: String,
    val href: String) {

    fun getURL(): String {
        return "$baseUrl$href$archtecture"
    }
}

private fun String.cleanupVersion(): String {
    var text = this

    if (text.contains("v"))
        text = text.substring(1, text.length)

    if (text.contains("-"))
        text = text.substring(0, text.indexOf("-"))

    text = text.replace("/", "")

    return text
}

private fun Element.toVersionInfo(): VersionInfo {
    val version = this.select("td:nth-child(2) > a")
        .text()
        .cleanupVersion()

    val href = this.select("td:nth-child(2) > a").attr("href")
    val releaseDate = this.select("td:nth-child(3)").text()

    return VersionInfo(
        Version(version),
        releaseDate,
        href
    )
}

private fun List<Element>.toVersionInfoList(): List<VersionInfo> {
    return this.map { it.toVersionInfo() }
}

private fun List<Element>.removeNonVersionEntry(): List<Element> {
    return this.filter { it.select("td:nth-child(2) > a").text().startsWith("v") }
}

private fun List<Element>.removeReleaseCandidate(): List<Element> {
    return this.filter { !it.select("td:nth-child(2) > a").text().contains("rc") }
}

fun getVersionList(): List<VersionInfo> {
    val doc = Jsoup.connect(baseUrl).timeout(60000).get();
    val elements = doc.select("body > table > tbody > tr");

    if (elements.isEmpty())
        return emptyList()

    return elements.toList()
        .removeNonVersionEntry()
        .removeReleaseCandidate()
        .toVersionInfoList()
}

fun getLinks(versionInfo: VersionInfo): List<String> {

    val doc = Jsoup.connect(versionInfo.getURL()).timeout(60000).get()
    val elements = doc.select("body > table > tbody > tr > td:nth-child(2) > a")

    return elements.filter { element -> element.text().contains(".deb") }
        .map { element -> element.attr("href") }
        .map { versionInfo.getURL() + "/" + it }

}

fun getCommand(versionInfo: VersionInfo, links: List<String>): String {
    val allLinks = links.joinToString(separator = " ")

    return """
        mkdir -p ~/Downloads/kernel/${versionInfo.version}
        cd ~/Downloads/kernel/${versionInfo.version}
        
        wget $allLinks
        
        sudo dpkg -i *.deb
    """.trimIndent()
}

fun main() {

    val currentKernelVersion = getKernelVersion()
    println("Versão atual: $currentKernelVersion")

    println("Obtendo lista de versões")
    val versionInfo = getVersionList().maxByOrNull { it.version }
        ?: run {
            println("Nenhuma nova versão")
            exitProcess(0)
        }

    println("Getting links to version ${versionInfo.version}")
    val links = getLinks(versionInfo)

    println("Para baixar execute os comandos")
    println(getCommand(versionInfo, links))
}
