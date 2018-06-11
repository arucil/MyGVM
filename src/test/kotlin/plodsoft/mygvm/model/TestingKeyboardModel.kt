package plodsoft.mygvm.model

import plodsoft.mygvm.keyboard.KeyboardModel
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
            out.println("[get key wait: $key]")
            key
        } else {
            val s = reader.readLine()
            if (s.isEmpty()) {
                out.println("[get key no wait: no key]")
                0
            } else {
                val key = Integer.parseInt(s)
                out.println("[get key no wait: $key]")
                key
            }
        }
    }

    override fun isKeyPressed(key: Int): Boolean {
        out.println("[is key pressed: $key]")
        return false
    }

    override fun getPressedKey(): Int {
        val s = reader.readLine()
        val key = if (s.isEmpty()) {
            0
        } else {
            Integer.parseInt(s)
        }
        out.println("[get pressed key: $key]")
        return key
    }

    override fun revalidateKey(key: Int) {
        out.println("[revalidate key $key]")
    }

    override fun revalidateAllKeys() {
        out.println("[revalidate all keys]")
    }
}