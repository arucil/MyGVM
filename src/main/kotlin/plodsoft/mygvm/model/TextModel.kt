package plodsoft.mygvm.model

class TextModel(private val backingRam: RamModel, private val startingAddress: Int, private val screenModel: ScreenModel) {
    companion object {
        const val LARGE_FONT_WIDTH = 8
        const val LARGE_FONT_HEIGHT = 16

        const val SMALL_FONT_WIDTH = 6
        const val SMALL_FONT_HEIGHT = 13

        enum class TextMode {
            LARGE_FONT, SMALL_FONT
        }
    }


    private var column: Int = 0
    private var row: Int = 0
    private var rows: Int = -1
    private var columns: Int = -1

    var textMode: TextMode = TextMode.LARGE_FONT
        /**
         * 清除文本缓冲区并设置字体大小
         */
        set(newMode) {
            field = newMode
            row = 0
            column = 0

            when (newMode) {
                TextMode.LARGE_FONT -> {
                    columns = ScreenModel.WIDTH / LARGE_FONT_WIDTH
                    rows = ScreenModel.HEIGHT / LARGE_FONT_HEIGHT
                }
                TextMode.SMALL_FONT -> {
                    columns = ScreenModel.WIDTH / SMALL_FONT_WIDTH
                    rows = ScreenModel.HEIGHT / SMALL_FONT_HEIGHT
                }
            }

            (0 until rows * columns).forEach {
                backingRam.setByte(startingAddress + it, 0)
            }
        }

    init {
        textMode = TextMode.LARGE_FONT
    }

    /**
     * 不刷新屏幕
     */
    fun addByte(byte: Byte) {
    }

    /**
     * 不刷新屏幕
     */
    fun addBytes(bytes: ByteArray) {
    }

    /**
     * row和column均从0开始，若超过范围则自动调整row/column值
     */
    fun setLocation(row: Int, column: Int) {
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
     * @param renderRows 从bit0-bitN表示该行文本是否刷新，如果为0则刷新
     */
    fun renderToScreen(renderRows: Int) {
    }
}