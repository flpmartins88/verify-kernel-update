@Grab("org.jsoup:jsoup:1.7.3")
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

def baseUrl = "http://kernel.ubuntu.com/~kernel-ppa/mainline/"

def currentVersion
def versionList = []

if (args) {
    currentVersion = new Version(args[0])
} else {
    def value = Utils.getKernelVersion()
    value = value.substring(0, value.indexOf("-"))
    currentVersion = new Version(value)
}

println "Versão atual: ${currentVersion.get()}"
println "Obtendo lista"

Document doc = Jsoup.connect(baseUrl).timeout(60000).get();
Elements elements = doc.select("body > table > tbody > tr");

if (elements.isEmpty()) {
    println "Não encontrei nada. Saindo..."
    return
}

def version, releaseDate, href

println "Procurando novas versões"

for (int linha = 0; linha < elements.size(); linha++) {

    version     = elements.get(linha).select("td:nth-child(2) > a").text()
    href        = elements.get(linha).select("td:nth-child(2) > a").attr("href")
    releaseDate = elements.get(linha).select("td:nth-child(3)").text()

    // Tratar as versões
    if (!version || !version.startsWith("v"))
        continue

    if (version.contains("rc"))
        continue

    // println version + " " + version.indexOf("-")
    if (version.contains("-"))
        version = version.substring(0, version.indexOf("-"))

    version = version.replace("/", "")
    version = version.replace("v", "")

    href = baseUrl + href

    versionList += [version: new Version(version), releaseDate: releaseDate, href: href]

    //println "Version: ${version} Release Date: ${releaseDate}"
}

def newVersion

versionList
    .sort { 
        it.version
    }
    .each {
        if (it.version.compareTo(currentVersion) == 1) {
            newVersion = it

            // se dar um break aqui ele vai parar de procurar e vai mostrar somente a próxima, não a mais recente no caso de mais de uma
        }
}

if (newVersion) {
    println "Nova versão: ${newVersion.version.get()} lançada em: ${newVersion.releaseDate}"
    println "URL: ${newVersion.href}"
} else {
    println "Não existe nenhuma nova versão"
}
