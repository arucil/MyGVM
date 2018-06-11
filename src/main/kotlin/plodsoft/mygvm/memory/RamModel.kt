package plodsoft.mygvm.memory


/**
 * 内存model
 */
interface RamModel : ReadableMemory, WritableMemory {
    companion object {
        const val SIZE = 65536
    }

    fun getUint8(address: Int): Int = getByte(address).toInt() and 0xff
    fun getUint16(address: Int): Int
    fun getInt16(address: Int): Int
    fun getUint24(address: Int): Int
    fun getInt32(address: Int): Int

    fun setUint8(address: Int, value: Int) = setByte(address, value.toByte())
    fun setUint16(address: Int, value: Int) = setInt16(address, value)
    fun setInt16(address: Int, value: Int)
    fun setUint24(address: Int, value: Int)
    fun setInt32(address: Int, value: Int)


    /**
     * 两块内存区域可以重叠
     */
    fun copy(destAddress: Int, srcAddress: Int, count: Int) {
        if (srcAddress >= destAddress) {
            for (i in 0 until count) {
                setByte(destAddress + i, getByte(srcAddress + i))
            }
        } else {
            for (i in (count - 1) downTo 0) {
                setByte(destAddress + i, getByte(srcAddress + i))
            }
        }
    }
}