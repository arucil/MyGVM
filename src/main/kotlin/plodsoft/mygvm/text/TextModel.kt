package plodsoft.mygvm.text

/**
 * 文本缓冲区model
 */
interface TextModel {

    enum class TextMode {
        LARGE_FONT, SMALL_FONT
    }


    /**
     * 获取/设置 字体模式
     */
    var textMode: TextMode

    /**
     * 清除屏幕并复位坐标
     */
    fun clear()

    /**
     * 不刷新屏幕
     */
    fun addByte(byte: Byte)

    /**
     * 不刷新屏幕
     */
    fun addBytes(bytes: ByteArray)

    /**
     * 设置追加文本的坐标
     *
     * row和column均从0开始，若超过范围则自动调整row/column值
     */
    fun setLocation(row: Int, column: Int)

    /**
     * 把文本渲染到屏幕
     *
     * @param renderRows 从bit0-bitN表示该行文本是否刷新，如果为0则刷新
     */
    fun renderToScreen(renderRows: Int)
}