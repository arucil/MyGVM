package plodsoft.mygvm

import java.io.InputStream
import java.nio.charset.Charset

class TestingInputStream : InputStream() {
    private var data = ByteArray(0)
    private var pos = 0

    fun resetInput(input: String) {
        pos = 0
        data = input.toByteArray(Charset.forName("gb2312"))
    }

    override fun read(): Int =
        if (pos >= data.size) -1
        else data[pos++].toInt() and 0xff

    override fun available(): Int =
        if (pos >= data.size) 0 else 1
}