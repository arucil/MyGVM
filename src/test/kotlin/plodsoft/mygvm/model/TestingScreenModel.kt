package plodsoft.mygvm.model

import plodsoft.mygvm.util.Rect
import java.io.OutputStream
import java.io.PrintStream
import java.nio.charset.Charset

class TestingScreenModel(outputStream: OutputStream) : ScreenModel {
    private val out = PrintStream(outputStream)

    override var dirtyRegion: Rect = Rect.EMPTY
        get() = TODO("not implemented")

    override fun clear() {
        out.println("[clear screen]")
    }

    override fun clearBuffer() {
        out.println("[clear buffer]")
    }

    override fun drawData(x: Int, y: Int, width: Int, height: Int, mem: ReadableMemory, address: Int, mode: Int) {
        out.println("[write block $x,$y,$width,$height,$address,${mode.toString(16)}]")
    }

    override fun drawString(x: Int, y: Int, str: ByteArray, font: TextModel.TextMode, mode: Int) {
        out.println("[text out $x,$y,${String(str, Charset.forName("gb2312"))},$font,${mode.toString(16)}]")
    }

    override fun drawRect(x: Int, y: Int, x1: Int, y1: Int, fill: Boolean, mode: Int) {
        out.println("[draw rect $x,$y,$x1,$y1,$fill,${mode.toString(16)}]")
    }

    override fun renderBufferToGraphics() {
        out.println("[refresh]")
    }
}