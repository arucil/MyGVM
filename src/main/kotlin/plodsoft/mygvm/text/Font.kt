package plodsoft.mygvm.text

import plodsoft.mygvm.memory.ReadableMemory
import plodsoft.mygvm.util.readAll

/**
 * 负责加载字体文件、根据文字gb2312编码获取字体数据
 */
object Font {
    private const val LARGE_ASCII_FONT_BYTES = (DefaultTextModel.LARGE_FONT_WIDTH + 7) / 8 * DefaultTextModel.LARGE_FONT_HEIGHT
    private const val LARGE_GB_FONT_BYTES = (DefaultTextModel.LARGE_FONT_WIDTH * 2 + 7) / 8 * DefaultTextModel.LARGE_FONT_HEIGHT
    private const val SMALL_ASCII_FONT_BYTES = (DefaultTextModel.SMALL_FONT_WIDTH + 7) / 8 * DefaultTextModel.SMALL_FONT_HEIGHT
    private const val SMALL_GB_FONT_BYTES = (DefaultTextModel.SMALL_FONT_WIDTH * 2 + 7) / 8 * DefaultTextModel.SMALL_FONT_HEIGHT

    /**
     * 所有字体数据都保存在同一个数组中
     */
    private val data: ByteArray

    private val offsetEmpty: Int
    private val offsetAscii8: Int
    private val offsetAscii6: Int
    private val offsetGB16: Int
    private val offsetGB12: Int

    init {
        val ascii8 = javaClass.getResourceAsStream("/ascii8.bin").readAll()
        val ascii6 = javaClass.getResourceAsStream("/ascii6.bin").readAll()
        val gb16 = javaClass.getResourceAsStream("/gbfont16.bin").readAll()
        val gb12 = javaClass.getResourceAsStream("/gbfont12.bin").readAll()

        val emptyDataSize = LARGE_GB_FONT_BYTES
        offsetEmpty = 0

        data = ByteArray(emptyDataSize + ascii8.size + ascii6.size + gb16.size + gb12.size)

        offsetAscii8 = emptyDataSize
        System.arraycopy(ascii8, 0, data, offsetAscii8, ascii8.size)

        offsetAscii6 = offsetAscii8 + ascii8.size
        System.arraycopy(ascii6, 0, data, offsetAscii6, ascii6.size)

        offsetGB16 = offsetAscii6 + ascii6.size
        System.arraycopy(gb16, 0, data, offsetGB16, gb16.size)

        offsetGB12 = offsetGB16 + gb16.size
        System.arraycopy(gb12, 0, data, offsetGB12, gb12.size)
    }

    private object Mem : ReadableMemory {
        override fun getByte(offset: Int): Byte = data[offset]
    }

    /**
     * 获取字体数据
     */
    val fontMem: ReadableMemory = Mem

    /**
     * 根据gb2312编码获取字体数据在 fontMem 中的地址
     *
     * 如果没有该编码的字体数据, 则仍然会返回一个合法地址, 但地址所指向的数据是空白
     *
     * @param ch 只用到低 16 bit. 如果是全角字符, 则第二个字节在bit8 ~ bit15
     */
    fun getFontDataOffset(ch: Int, font: TextModel.TextMode): Int =
        if (font == TextModel.TextMode.LARGE_FONT) {
            getFontDataOffsetAux(ch, offsetAscii8, LARGE_ASCII_FONT_BYTES, offsetGB16, offsetGB12 - offsetGB16, LARGE_GB_FONT_BYTES)
        } else {
            getFontDataOffsetAux(ch, offsetAscii6, SMALL_ASCII_FONT_BYTES, offsetGB12, data.size - offsetGB12, SMALL_GB_FONT_BYTES)
        }

    private inline fun getFontDataOffsetAux(ch: Int, asciiOffset: Int, asciiBytes: Int, gbOffset: Int, gbSize: Int, gbBytes: Int) =
            if (ch <= 0xff) { // 半角
                asciiOffset + ch * asciiBytes
            } else { // 全角
                var b1 = (ch and 0xff) - 0xa1
                if (b1 > 8) {
                    b1 -= 6
                }
                val b2 = (ch ushr 8) - 0xa1
                val offset = (b1 * 94 + b2) * gbBytes
                if (offset in 0 until gbSize) {
                    offset + gbOffset
                } else {
                    0
                }
            }
}