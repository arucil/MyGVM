package plodsoft.mygvm.screen

import plodsoft.mygvm.memory.ReadableMemory
import plodsoft.mygvm.memory.WritableMemory
import plodsoft.mygvm.text.TextModel
import plodsoft.mygvm.util.Rect


/**
 * 屏幕和屏幕缓冲区model
 *
 * 其中的绘图函数默认在缓冲区绘制
 */
interface ScreenModel {
    companion object {
        const val WIDTH = 160
        const val HEIGHT = 80
        const val BYTE_WIDTH = WIDTH / 8
        const val RAM_SIZE = BYTE_WIDTH * HEIGHT
    }

    /**
     * 用于drawData, drawString
     */
    enum class DataDrawMode {
        Copy, Not, Or, And, Xor
    }

    /**
     * 用于形状绘制(point, line, rectangle, oval)
     */
    enum class ShapeDrawMode {
        Clear, Normal, Invert
    }

    /**
     * 屏幕的更新区域
     */
    val dirtyRegion: Rect


    enum class Target {
        Graphics, // 在屏幕绘图
        Buffer    // 在缓冲区绘图
    }

    /**
     * 后续的绘图操作的目标: 直接在屏幕绘图或在缓冲区绘图
     */
    var target: Target

    /**
     * 清空屏幕和缓冲区，重置target为Graphics
     */
    fun reset()

    /**
     * 清空屏幕或缓冲区
     */
    fun clear()

    /**
     * 从内存读取图形数据进行绘制
     * @param x
     * @param y
     * @param width
     * @param height
     * @param mem
     * @param addr
     * @param mode DataDrawMode
     * @param horizontalMirrored 是否左右反转
     * @param inverse 是否反显
     */
    fun drawData(x: Int, y: Int, width: Int, height: Int, mem: ReadableMemory, addr: Int,
                 mode: DataDrawMode, horizontalMirrored: Boolean, inverse: Boolean)

    /**
     * 从屏幕或缓冲区读取图形数据保存到内存
     *
     * x, y, width和height参数的范围0~0xffff
     */
    fun saveData(x: Int, y: Int, width: Int, height: Int, mem: WritableMemory, addr: Int)

    /**
     * 从mem的addr地址开始读取len个字节文本并绘制
     * @param font 大字体/小字体
     * @param mode DataDrawMode
     * @param horizontalMirrored 是否左右反转
     * @param inverse 是否反显
     */
    fun drawString(x: Int, y: Int, mem: ReadableMemory, addr: Int, len: Int, font: TextModel.TextMode,
                   mode: DataDrawMode, horizontalMirrored: Boolean, inverse: Boolean)

    /**
     * 绘制矩形
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param fill 是否填充
     * @param mode ShapeDrawMode
     */
    fun drawRect(x1: Int, y1: Int, x2: Int, y2: Int, fill: Boolean, mode: ShapeDrawMode)

    /**
     * 画线
     * @param mode ShapeDrawMode
     */
    fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int, mode: ShapeDrawMode)

    /**
     *　画椭圆
     * @param mode ShapeDrawMode
     */
    fun drawOval(cx: Int, cy: Int, a: Int, b: Int, fill: Boolean, mode: ShapeDrawMode)

    /**
     * 画点
     * @param x
     * @param y
     * @param mode ShapeDrawMode
     */
    fun drawPoint(x: Int, y: Int, mode: ShapeDrawMode)


    enum class ScrollDirection {
        Left, // 屏幕往左滚动
        Right // 屏幕往右滚动
    }

    /**
     * 缓冲区滚动一个像素
     */
    fun scroll(dir: ScrollDirection)


    enum class MirrorDirection {
        Vertical,
        Horizontal
    }

    /**
     * 缓冲区翻转
     */
    fun mirror(dir: MirrorDirection)

    /**
     * 检测某点是否存在, 若存在则返回非零值
     */
    fun testPoint(x: Int, y: Int): Int

    /**
     * 把缓冲区的内容刷新到屏幕
     */
    fun renderBufferToGraphics()
}