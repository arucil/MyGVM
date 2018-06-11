package plodsoft.mygvm.model

import java.io.ByteArrayOutputStream

interface ReadableMemory {
    fun getByte(address: Int): Byte

    /**
     * 从address读取以\0结尾的字符串
     */
    fun getString(address: Int): ByteArray =
        ///TODO: 检查字符串没有\0结尾
        with (ByteArrayOutputStream()) {
            var i = address
            while (true) {
                val b = getByte(i++)
                if (b == 0.toByte()) {
                    break
                }
                write(b.toInt())
            }
            toByteArray()
        }
}