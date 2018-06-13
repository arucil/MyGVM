package plodsoft.mygvm.model

import plodsoft.mygvm.memory.ReadableMemory
import plodsoft.mygvm.memory.WritableMemory
import plodsoft.mygvm.screen.ScreenModel
import plodsoft.mygvm.text.TextModel
import plodsoft.mygvm.util.Rect
import java.io.OutputStream
import java.io.PrintStream
import java.nio.charset.Charset

class TestingScreenModel(outputStream: OutputStream) : ScreenModel {
    private val out = PrintStream(outputStream)

    override var target = ScreenModel.Target.Graphics

    override var dirtyRegion: Rect = Rect.EMPTY
        get() = TODO("not implemented")

    private inline fun targetStr() = if (target == ScreenModel.Target.Graphics) "screen" else "buffer"

    private inline fun Boolean.fillStr() = if (this) "fill" else "no-fill"

    private inline fun Boolean.mirrorStr() = if (this) "mirror" else "no-mirror"

    private inline fun Boolean.invertStr() = if (this) "invert" else "no-invert"

    override fun clear() {
        out.println("[clear ${targetStr()}]")
    }

    override fun drawData(x: Int, y: Int, width: Int, height: Int, mem: ReadableMemory, addr: Int,
                          mode: ScreenModel.DataDrawMode, horizontalMirrored: Boolean, inverse: Boolean) {
        out.println("[write block $x, $y, $width, $height, 0x${addr.toString(16)}, ${targetStr()}," +
                " ${mode.name.toLowerCase()}, ${horizontalMirrored.mirrorStr()}, ${inverse.invertStr()}]")
    }

    override fun drawString(x: Int, y: Int, mem: ReadableMemory, addr: Int, len: Int, font: TextModel.TextMode,
                            mode: ScreenModel.DataDrawMode, horizontalMirrored: Boolean, inverse: Boolean) {
        out.println("[text out $x, $y, ${String(mem.getString(addr), Charset.forName("gb2312"))}, $font," +
                " ${targetStr()}, ${mode.name.toLowerCase()}, ${horizontalMirrored.mirrorStr()}, ${inverse.invertStr()}]")
    }

    override fun drawRect(x: Int, y: Int, x1: Int, y1: Int, fill: Boolean, mode: ScreenModel.ShapeDrawMode) {
        out.println("[draw rect $x, $y, $x1, $y1, ${fill.fillStr()}, ${targetStr()}, ${mode.name.toLowerCase()}]")
    }

    override fun renderBufferToGraphics() {
        out.println("[refresh]")
    }

    override fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int, mode: ScreenModel.ShapeDrawMode) {
        out.println("[draw line $x1, $y1, $x2, $y2, ${targetStr()}, ${mode.name.toLowerCase()}]")
    }

    override fun drawOval(cx: Int, cy: Int, a: Int, b: Int, fill: Boolean, mode: ScreenModel.ShapeDrawMode) {
        out.println("[draw oval $cx, $cy, $a, $b, ${fill.fillStr()}, ${targetStr()}, ${mode.name.toLowerCase()}]")
    }

    override fun drawPoint(x: Int, y: Int, mode: ScreenModel.ShapeDrawMode) {
        out.println("[draw point $x, $y, ${targetStr()}, ${mode.name.toLowerCase()}]")
    }

    override fun testPoint(x: Int, y: Int): Int {
        out.println("[test point $x, $y, ${targetStr()}]")
        return if (x in 0..159 && y in 0..79) {
            0
        } else {
            1
        }
    }

    override fun saveData(x: Int, y: Int, width: Int, height: Int, mem: WritableMemory, addr: Int) {
        out.println("[get block $x, $y, $width, $height, ${targetStr()}, 0x${addr.toString(16)}]")
    }

    override fun scroll(dir: ScreenModel.ScrollDirection) {
        out.println("[scroll ${dir.name.toLowerCase()}, ${targetStr()}]")
    }

    override fun mirror(dir: ScreenModel.MirrorDirection) {
        out.println("[mirror ${dir.name.toLowerCase()}, ${targetStr()}]")
    }
}