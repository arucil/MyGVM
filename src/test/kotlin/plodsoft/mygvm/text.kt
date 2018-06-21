package plodsoft.mygvm

import java.nio.charset.Charset
import java.util.*

fun main(arfs: Array<String>) {
    println(Arrays.toString("令:".toByteArray(Charset.forName("gb2312")).map { (it.toInt() and 0xff).toString(16) }.toTypedArray()))
    println(String("bd f8 b9 a5".split(' ').map { Integer.parseInt(it, 16).toByte() }.toByteArray(), Charset.forName("gb2312")))
}