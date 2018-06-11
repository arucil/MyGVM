package plodsoft.mygvm.model

import plodsoft.mygvm.util.Rect
import java.io.OutputStream
import java.io.PrintStream
import java.nio.charset.Charset

class TestingScreenModel(outputStream: OutputStream) : ScreenModel {
    private val out = PrintStream(outputStream)

    override var dirtyRegion: Rect = Rect.EMPTY
        get() = TODO("not implemented")

    override fun clearGraphics() {
        out.println("[clear screen]")
    }

    override fun clearBuffer() {
        out.println("[clear buffer]")
    }

    override fun drawData(x: Int, y: Int, width: Int, height: Int, mem: ReadableMemory, address: Int, mode: Int) {
        out.println("[write block $x,$y,$width,$height,0x${address.toString(16)},0x${mode.toString(16)}]")
    }

    override fun drawString(x: Int, y: Int, mem: ReadableMemory, addr: Int, len: Int, font: TextModel.TextMode, mode: Int) {
        out.println("[text out $x,$y,${String(mem.getString(addr), Charset.forName("gb2312"))},$font,0x${mode.toString(16)}]")
    }

    override fun drawRect(x: Int, y: Int, x1: Int, y1: Int, fill: Boolean, mode: Int) {
        out.println("[draw rect $x,$y,$x1,$y1,$fill,0x${mode.toString(16)}]")
    }

    override fun renderBufferToGraphics() {
        out.println("[refresh]")
    }

    override fun drawLine(x: Int, y: Int, x1: Int, y1: Int, mode: Int) {
        out.println("[draw line $x,$y,$x1,$y1,0x${mode.toString(16)}]")
    }

    override fun drawOval(x: Int, y: Int, rx: Int, ry: Int, fill: Boolean, mode: Int) {
        out.println("[draw oval $x,$y,$rx,$ry,$fill,0x${mode.toString(16)}]")
    }

    override fun drawPoint(x: Int, y: Int, mode: Int) {
        out.println("[draw point $x,$y,0x${mode.toString(16)}]")
    }

    override fun testPoint(x: Int, y: Int): Int {
        out.println("[test point $x,$y]")
        return if (x in 0..159 && y in 0..79) {
            0
        } else {
            1
        }
    }

    override fun saveData(x: Int, y: Int, width: Int, height: Int, isFromGraphics: Boolean, mem: WritableMemory, address: Int) {
        out.println("[get block $x,$y,$width,$height,$isFromGraphics,0x${address.toString(16)}]")
    }

    override fun scroll(dir: ScreenModel.ScrollDirection) {
        out.println("[scroll $dir]")
    }

    override fun mirror(dir: ScreenModel.MirrorDirection) {
        out.println("[mirror $dir]")
    }
}