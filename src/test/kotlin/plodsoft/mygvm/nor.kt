package plodsoft.mygvm

private fun normalizePath(path: String): String =
        ArrayList<String>().let { res ->
            path.split('/').forEach {
                when (it) {
                    "", "." -> {}
                    ".." -> {
                        if (res.isNotEmpty()) {
                            res.removeAt(res.size - 1)
                        }
                    }
                    else -> res.add(it)
                }
            }
            buildString {
                append('/')
                res.forEach {
                    append(it).append('/')
                }
            }
        }

fun main(a: Array<String>) {
    println(normalizePath("/"))
    println(normalizePath("/./"))
    println(normalizePath("/../"))
    println(normalizePath("/Lava"))
    println(normalizePath("/Lava/"))
    println(normalizePath("/Lava/."))
    println(normalizePath("/Lava/./"))
    println(normalizePath("/Lava/.."))
    println(normalizePath("/Lava/../"))
    println(normalizePath("/Lava/More/../"))
    println(normalizePath("/Lava/More/../.."))
    println(normalizePath("/Lava/More/Less/../"))
}