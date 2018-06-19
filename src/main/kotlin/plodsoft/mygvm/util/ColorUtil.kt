package plodsoft.mygvm.util

/**
 * 解析#rrggbb或#rgb格式的颜色字符串
 *
 * @return 0xrrggbb
 *
 * @throws IllegalArgumentException
 */
fun String.parseColor(): Int {
    if (!matches("^#([a-fA-F0-9]{3}|[a-fA-F0-9]{6})$".toRegex())) {
        throw IllegalArgumentException("Invalid color string")
    }

    return if (length == 4) {
        val r = Character.digit(this[1], 16)
        val g = Character.digit(this[2], 16)
        val b = Character.digit(this[3], 16)
        (r shl 20) or (r shl 16) or (g shl 12) or (g shl 8) or (b shl 4) or b
    } else {
        val r = Character.digit(this[1], 16) shl 4 or Character.digit(this[2], 16)
        val g = Character.digit(this[3], 16) shl 4 or Character.digit(this[4], 16)
        val b = Character.digit(this[5], 16) shl 4 or Character.digit(this[6], 16)
        (r shl 16) or (g shl 8) or b
    }
}
