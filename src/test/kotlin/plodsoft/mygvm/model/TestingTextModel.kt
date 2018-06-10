package plodsoft.mygvm.model

import java.io.OutputStream
import java.io.PrintStream

class TestingTextModel(val outputStream: OutputStream) : TextModel {
    private val out = PrintStream(outputStream)

    override var textMode: TextModel.TextMode = TextModel.TextMode.LARGE_FONT
        get() {
            out.println("[get text mode]")
            return field
        }
        set(value) {
            out.println("[set text mode to $value]")
            field = value
        }

    override fun addByte(byte: Byte) {
        out.println("[add byte ${(byte.toInt() and 0xff).toChar()}]")
    }

    override fun addBytes(bytes: ByteArray) {
        out.println("[add bytes (${String(bytes)})]")
    }

    override fun setLocation(row: Int, column: Int) {
        out.println("[locate to (x: $column, y: $row)]")
    }

    override fun renderToScreen(renderRows: Int) {
        out.println("[render text to screen: 0b${renderRows.toString(2)}]")
    }
}