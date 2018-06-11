package plodsoft.mygvm.screen

import plodsoft.mygvm.memory.RamSegment
import plodsoft.mygvm.memory.ReadableMemory
import plodsoft.mygvm.memory.WritableMemory
import plodsoft.mygvm.text.TextModel
import plodsoft.mygvm.screen.ScreenModel.Companion.BYTE_WIDTH
import plodsoft.mygvm.util.Rect
import plodsoft.mygvm.util.between

class DefaultScreenModel(private val graphicsRam: RamSegment, private val bufferRam: RamSegment) : ScreenModel {
    companion object {
        private val BIT_MASK = intArrayOf(0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01)
    }

    override val dirtyRegion: Rect
        get() = TODO("not implemented")

    override fun clearGraphics() {
        graphicsRam.zero()
    }

    override fun clearBuffer() {
        bufferRam.zero()
    }

    override fun drawData(x: Int, y: Int, width: Int, height: Int, mem: ReadableMemory, addr: Int, mode: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun saveData(x: Int, y: Int, width: Int, height: Int, isFromGraphics: Boolean, mem: WritableMemory, addr: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun drawString(x: Int, y: Int, mem: ReadableMemory, addr: Int, len: Int, font: TextModel.TextMode, mode: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun drawRect(x1: Int, y1: Int, x2: Int, y2: Int, fill: Boolean, mode: Int) {
        if (fill) {
            for (x in between(x1, x2)) {
                vertLine(x, y1, y2, mode)
            }
        } else {
        }
    }

    private inline fun vertLine(x: Int, y1: Int, y2: Int, mode: Int) {
        for (y in between(y1, y2)) {
            point(x, y, mode)
        }
    }

    // 如果x2小于x1则不绘制
    private inline fun line(x1: Int, y1: Int, x2: Int, y2: Int) {
    }

    override fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int, mode: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun drawOval(x: Int, y: Int, rx: Int, ry: Int, fill: Boolean, mode: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun drawPoint(x: Int, y: Int, mode: Int) {
        point(x, y, mode)
    }

    private inline fun point(x: Int, y: Int, mode: Int) {
        if (x in 0 until ScreenModel.WIDTH && y in 0 until ScreenModel.HEIGHT) {
            val ram = if ((mode and ScreenModel.DrawMode.GRAPHICS_DRAW_MASK) != 0) graphicsRam else bufferRam
            val offset = y * BYTE_WIDTH + (x ushr 3)
            val b = ram.getByte(offset).toInt()
            ram.setByte(offset,
                    when (mode and ScreenModel.ShapeDrawMode.DRAW_MODE_MASK) {
                        ScreenModel.ShapeDrawMode.CLEAR -> b and BIT_MASK[x and 0x7].inv()
                        ScreenModel.ShapeDrawMode.NORMAL -> b or BIT_MASK[x and 0x7]
                        ScreenModel.ShapeDrawMode.INVERT -> b xor BIT_MASK[x and 0x7]
                        else -> b
                    }.toByte())
        }
    }

    override fun scroll(dir: ScreenModel.ScrollDirection) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun mirror(dir: ScreenModel.MirrorDirection) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun testPoint(x: Int, y: Int): Int =
        if (x in 0 until ScreenModel.WIDTH && y in 0 until ScreenModel.HEIGHT) {
            graphicsRam.getByte(y * BYTE_WIDTH + (x ushr 3)).toInt() and BIT_MASK[y and 0x7]
        } else
            0

    override fun renderBufferToGraphics() {
        for (i in 0 until bufferRam.size) {
            graphicsRam.setByte(i, bufferRam.getByte(i))
        }
    }
}