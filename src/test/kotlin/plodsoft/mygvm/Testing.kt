package plodsoft.mygvm

import plodsoft.mygvm.runtime.Runtime
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset


fun Runtime.test(name: String, inputStream: TestingInputStream, outputStream: ByteArrayOutputStream, code: IntArray, input: String, output: String) {
    loadCode(ByteArrayInputStream(code.map { it.toByte() }.toByteArray()))

    inputStream.resetInput(input)
    outputStream.reset()

    prepare()

    while (!runOneStep()) {
    }

    cleanUp()

    val actual = String(outputStream.toByteArray(), Charset.forName("gb2312"))
    if (actual != output) {
        System.err.println("""======================Test case [$name] failed:
            |expected output:
            |{$output}
            |actual output:
            |{$actual}
            |
        """.trimMargin())
    }
}
