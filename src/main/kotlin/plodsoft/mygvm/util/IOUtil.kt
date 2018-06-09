package plodsoft.mygvm.util

import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * @throws java.io.IOException
 */
fun InputStream.readAll(): ByteArray =
    with (ByteArrayOutputStream()) {
        val buffer = ByteArray(1024)
        var count = read(buffer)
        while (count >= 0) {
            write(buffer, 0, count)
        }
        toByteArray()
    }
