package plodsoft.mygvm.memory

import java.io.ByteArrayOutputStream

interface ReadableMemory {
    fun getByte(offset: Int): Byte

    /**
     * 从offset读取以\0结尾的字符串
     */
    fun getString(offset: Int): ByteArray =
        ///TODO: 检查字符串没有\0结尾
        with (ByteArrayOutputStream()) {
            var i = offset
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