package plodsoft.mygvm.model

import plodsoft.mygvm.util.Rect


/**
 * 其中的绘图函数默认在缓冲区绘制
 */
interface ScreenModel {
    companion object {
        const val WIDTH = 160
        const val HEIGHT = 80
        const val BITS_PER_PIXEL = 1
    }

    object DrawMode {
        const val GRAPHICS_DRAW_MASK = 0x40 // 直接在屏幕上绘图
    }

    /**
     * 用于drawBytes, drawString
     */
    object DataDrawMode {
        const val HORIZONTAL_MIRROR_MASK = 0x20

        const val DRAW_MODE_MASK = 0x7
        const val COPY = 1
        const val NOT = 2
        const val OR = 3
        const val AND = 4
        const val XOR = 5
    }

    /**
     * 用于形状绘制(point, line, rectangle, oval)
     */
    object ShapeDrawMode {
        const val DRAW_MODE_MASK = 3
        const val CLEAR = 0
        const val NORMAL = 1
        const val INVERT = 2
    }

    /**
     * 屏幕的更新区域
     */
    val dirtyRegion: Rect

    /**
     * 清空屏幕
     */
    fun clear()

    /**
     * 清空缓冲区
     */
    fun clearBuffer()

    /**
     * 从内存读取图形数据进行绘制
     * @param x
     * @param y
     * @param width
     * @param height
     * @param mem
     * @param address
     * @param mode DrawMode, DataDrawMode
     */
    fun drawBytes(x: Int, y: Int, width: Int, height: Int, mem: ReadableMemory, address: Int, mode: Int)

    /**
     * 绘制文字
     * @param x
     * @param y
     * @param str
     * @param font 大字体/小字体
     * @mode mode DrawMode, DataDrawMode
     */
    fun drawString(x: Int, y: Int, str: ByteArray, font: TextModel.TextMode, mode: Int)

    /**
     * 绘制矩形
     * @param x
     * @param y
     * @param x1
     * @param y1
     * @param fill 是否填充
     * @param mode DrawMode, ShapeDrawMode
     */
    fun drawRect(x: Int, y: Int, x1: Int, y1: Int, fill: Boolean, mode: Int)

    /**
     * 画线
     * @param mode DrawMode, ShapeDrawMode
     */
    fun drawLine(x: Int, y: Int, x1: Int, y1: Int, mode: Int)

    /**
     *　画椭圆
     * @param mode DrawMode, ShapeDrawMode
     */
    fun drawOval(x: Int, y: Int, rx: Int, ry: Int, fill: Boolean, mode: Int)

    /**
     * 画点
     * @param x
     * @param y
     * @param mode DrawMode, ShapeDrawMode
     */
    fun drawPoint(x: Int, y: Int, mode: Int)

    /**
     * 检测某点是否存在, 若存在则返回非零值
     */
    fun testPoint(x: Int, y: Int): Int

    /**
     * 把缓冲区的内容刷新到屏幕
     */
    fun renderBufferToGraphics()
}