package plodsoft.mygvm.util

import java.io.*

/**
 * @throws java.io.IOException
 */
fun InputStream.readAll(): ByteArray =
    with (ByteArrayOutputStream()) {
        val buffer = ByteArray(1024)
        while (true) {
            val count = read(buffer)
            if (count < 0) {
                break
            }
            write(buffer, 0, count)
        }
        toByteArray()
    }

/**
 * @throws java.io.IOException
 */
fun File.readAll(): ByteArray =
    BufferedInputStream(FileInputStream(this)).use {
        it.readAll()
    }
