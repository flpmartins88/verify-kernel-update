
class Version (val number: String) : Comparable<Version?> {

    init {
        require(number.matches("\\d+(\\.\\d+)*".toRegex())) { "Invalid version format" }
    }

    override fun compareTo(other: Version?): Int {
        if (other == null) return 1

        val thisParts = this.number.toVersionArray()
        val otherParts = other.number.toVersionArray()

        val length = Math.max(thisParts.size, otherParts.size)

        for (i in 0 until length) {
            val thisPart = if (i < thisParts.size) thisParts[i].toInt() else 0
            val otherPart = if (i < otherParts.size) otherParts[i].toInt() else 0

            if (thisPart < otherPart) return -1
            if (thisPart > otherPart) return 1
        }

        return 0
    }

    private fun String.toVersionArray(): Array<String> {
        return this.split("\\.".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
    }

    override fun toString(): String {
        return number
    }
}

fun main() {
    println(Version("1.0.0").compareTo(Version("1.2.0")))

    arrayOf(Version("1.1.0"), Version("1.2.0"), Version("1.1.3"), Version("1.0.7"))
        .sortedArray()
        .forEach { println(it) }
}
