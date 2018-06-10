package plodsoft.mygvm.model

import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import java.nio.charset.Charset

class TestingKeyboardModel(inputStream: InputStream, outputStream: OutputStream) : KeyboardModel {
    private val reader = inputStream.bufferedReader(Charset.forName("gb2312"))
    private val out = PrintStream(outputStream)

    override fun getLastKey(wait: Boolean): Int {
        return if (wait) {
            val key = Integer.parseInt(reader.readLine())
            out.println("[get key $wait: $key]")
            key
        } else {
            0
        }
    }

    override fun isKeyPressed(key: Int): Boolean {
        out.println("[is key pressed]")
        return false
    }

    override fun getPressedKey(): Int {
        out.println("[get pressed key]")
        return 0
    }

    override fun revalidateKey(key: Int) {
        out.println("[revalidate key $key]")
    }

    override fun revalidateAllKeys() {
        out.println("[revalidate all keys]")
    }
}