package plodsoft.mygvm.text

import plodsoft.mygvm.memory.RamSegment
import plodsoft.mygvm.text.TextModel.TextMode
import plodsoft.mygvm.screen.ScreenModel

class DefaultTextModel(private val ram: RamSegment, private val screenModel: ScreenModel): TextModel {
    companion object {
        const val LARGE_FONT_WIDTH = 8
        const val LARGE_FONT_HEIGHT = 16
        const val LARGE_FONT_ROW_HEIGHT = 16

        const val LARGE_FONT_COLUMNS = ScreenModel.WIDTH / LARGE_FONT_WIDTH
        const val LARGE_FONT_ROWS = ScreenModel.HEIGHT / LARGE_FONT_ROW_HEIGHT

        const val SMALL_FONT_WIDTH = 6
        const val SMALL_FONT_HEIGHT = 12
        const val SMALL_FONT_ROW_HEIGHT = 13

        const val SMALL_FONT_COLUMNS = ScreenModel.WIDTH / SMALL_FONT_WIDTH
        const val SMALL_FONT_ROWS = ScreenModel.HEIGHT / SMALL_FONT_ROW_HEIGHT
    }


    private var column: Int = 0
    private var row: Int = 0
    private var rows: Int = -1
    private var columns: Int = -1
    private var rowHeight: Int = 0


    override var textMode: TextMode = TextMode.LARGE_FONT
        /**
         * 设置字体大小
         */
        set(newMode) {
            field = newMode
            row = 0
            column = 0

            when (newMode) {
                TextMode.LARGE_FONT -> {
                    columns = LARGE_FONT_COLUMNS
                    rows = LARGE_FONT_ROWS
                    rowHeight = LARGE_FONT_ROW_HEIGHT
                }
                TextMode.SMALL_FONT -> {
                    columns = SMALL_FONT_COLUMNS
                    rows = SMALL_FONT_ROWS
                    rowHeight = SMALL_FONT_ROW_HEIGHT
                }
            }
        }

    init {
        textMode = TextMode.LARGE_FONT
    }

    override fun clear() {
        row = 0
        column = 0
        ram.zero()
    }

    /**
     * 不刷新屏幕
     */
    override fun addByte(byte: Byte) {
        if (row >= rows) {
            scrollUp()
        }
        ram.setByte(row * columns + column,
                when (byte) {
                    0xd.toByte() -> return
                    0xa.toByte() -> {
                        column = 0
                        ++row
                        if (row >= rows) {
                            scrollUp()
                        }
                        return
                    }
                    0x9.toByte() -> 0x20
                    else -> byte
                })
        if (++column >= columns) {
            column = 0
            ++row
        }
    }

    /**
     * 文本上移一行
     */
    private inline fun scrollUp() {
        ram.copy(0, columns, (rows - 1) * columns)
        ram.fill((rows - 1) * columns, columns, 0x20)
        row = rows - 1
        column = 0
    }

    /**
     * 不刷新屏幕
     */
    override fun addBytes(bytes: ByteArray) {
        var i = 0
        while (i < bytes.size) {
            val b = bytes[i++]
            if (b >= 0) {
                addByte(b)
            } else {
                if (column == columns - 1) {
                    addByte(0x20)
                }
                addByte(b)
                if (i >= bytes.size) {
                    break
                }
                addByte(bytes[i++])
            }
        }
    }

    /**
     * row和column均从0开始，若超过范围则自动调整row/column值
     */
    override fun setLocation(row: Int, column: Int) {
        this.row = when {
            row < 0 -> 0
            row >= rows -> rows - 1
            else -> row
        }
        this.column = when {
            column < 0 -> 0
            column >= columns -> columns - 1
            else -> column
        }
    }

    /**
     * @param renderRows 从bit7-bit0表示从上到下的每一行文本是否刷新，如果都为0则全部刷新
     */
    override fun renderToScreen(renderRows: Int) {
        screenModel.target = ScreenModel.Target.Graphics

        if (textMode == TextMode.SMALL_FONT) {
            // 清除边界
            screenModel.drawLine(0, 0, ScreenModel.WIDTH - 1, 0, ScreenModel.ShapeDrawMode.Clear)
            screenModel.drawLine(0, ScreenModel.HEIGHT - 1, ScreenModel.WIDTH - 1, ScreenModel.HEIGHT - 1, ScreenModel.ShapeDrawMode.Clear)
            screenModel.drawRect(0, 0, 1, ScreenModel.HEIGHT - 1, true, ScreenModel.ShapeDrawMode.Clear)
            screenModel.drawRect(ScreenModel.WIDTH - 2, 0, ScreenModel.WIDTH - 1, ScreenModel.HEIGHT - 1, true, ScreenModel.ShapeDrawMode.Clear)

            // 清除每行文本的空隙
            for (y in (SMALL_FONT_HEIGHT + 1) until ScreenModel.HEIGHT step SMALL_FONT_ROW_HEIGHT) {
                screenModel.drawLine(0, y, ScreenModel.WIDTH - 1, y, ScreenModel.ShapeDrawMode.Clear)
            }
        }

        var mask = 0x100
        val y0 = if (textMode == TextMode.LARGE_FONT) 0 else 1
        val x0 = if (textMode == TextMode.LARGE_FONT) 0 else 2
        for (i in 0 until rows) {
            mask = mask ushr 1
            if ((mask and renderRows) != 0) {
                continue
            }
            screenModel.drawString(x0, y0 + i * rowHeight, ram, i * columns, columns, textMode, ScreenModel.DataDrawMode.Copy, false, false)
        }
    }
}