package plodsoft.mygvm.runtime

import plodsoft.mygvm.model.ReadableMemory
import plodsoft.mygvm.model.WritableMemory
import kotlin.experimental.xor


private val crc16Table = intArrayOf(0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50a5, 0x60c6, 0x70e7, 0x8108, 0x9129, 0xa14a, 0xb16b, 0xc18c, 0xd1ad, 0xe1ce, 0xf1ef)

/**
 * @return 0~0xffff
 */
fun ReadableMemory.getCrc16(address: Int, length: Int): Int {
    var addr = address
    var len = length
    var result = 0

    while (--len >= 0) {
        var tmp = result ushr 8 and 0xff
        result = result shl 4
        result = result xor crc16Table[tmp ushr 4 xor (getByte(addr).toInt() and 0xff ushr 4)]
        tmp = result ushr 8 and 0xff
        result = result shl 4
        result = result xor crc16Table[tmp ushr 4 xor (getByte(addr).toInt() and 0x0f)]
        addr++
    }
    return result and 0xffff
}

/**
 * 使用password对address地址开始的count个字节进行简单的xor加密
 */
inline fun <T> T.xorEncrypt(address: Int, count: Int, password: ByteArray)
        where T : ReadableMemory, T : WritableMemory {
    var p = 0
    for (i in address until (address + count)) {
        setByte(i, getByte(i) xor password[p++])
        if (p >= password.size) {
            p = 0
        }
    }
}


private val sine90Table = intArrayOf(
    0, 18, 36, 54, 71, 89, 107, 125, 143, 160, 178, 195, 213, 230, 248, 265, 282,
    299, 316, 333, 350, 367, 384, 400, 416, 433, 449, 465, 481, 496, 512, 527,
    543, 558, 573, 587, 602, 616, 630, 644, 658, 672, 685, 698, 711, 724, 737,
    749, 761, 773, 784, 796, 807, 818, 828, 839, 849, 859, 868, 878, 887, 896,
    904, 912, 920, 928, 935, 943, 949, 956, 962, 968, 974, 979, 984, 989, 994,
    998, 1002, 1005, 1008, 1011, 1014, 1016, 1018, 1020, 1022, 1023, 1023, 1024, 1024)

/**
 * 获取cosine值
 * @param degree 保留低15位(0 ~ 32767)
 * @return -1024 ~ 1024
 */
fun cos(degree: Int): Int {
    val deg = (degree and 0x7fff) % 360
    return if (deg >= 270)
        sin(deg - 270)
    else
        sin(deg + 90)
}

/**
 * 获取sine值
 * @param degree 保留低15位
 * @return -1024 ~ 1024
 */
fun sin(degree: Int): Int {
    val deg = (degree and 0x7fff) % 360
    return when (deg / 90) {
        0 -> sine90Table[deg]
        1 -> sine90Table[180 - deg]
        2 -> -sine90Table[deg - 180]
        else -> -sine90Table[360 - deg]
    }
}
