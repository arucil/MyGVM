package plodsoft.mygvm.util

data class Rect(val left: Int, val top: Int, val width: Int, val height: Int) {
    companion object {
        val EMPTY = Rect(0, 0, 0, 0)
    }

    val isEmpty = width == 0 || height == 0
}